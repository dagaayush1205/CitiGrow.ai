import numpy as np
import onnxruntime as ort
import soundfile as sf
from transformers import AutoTokenizer

# 1. Initialize Tokenizer (reusing HF to ensure accurate text-to-token conversion)
tokenizer = AutoTokenizer.from_pretrained(
    "ai4bharat/vits_rasa_13", trust_remote_code=True
)

text = "मेरा नाम आयुष है। यह एक टेस्ट है। क्या ऑडियो ठीक है?"
speaker_id = 16
emotion_id = 0

# Extract input IDs and convert to int64 numpy array
input_ids = tokenizer(text=text, return_tensors="np")["input_ids"].astype(np.int64)

print("Loading ONNX model...")
session = ort.InferenceSession("model.onnx")

# 2. Build the input dictionary dynamically based on the ONNX graph
expected_inputs = {inp.name: inp for inp in session.get_inputs()}
ort_inputs = {}

# Map text tokens
if "input_ids" in expected_inputs:
    ort_inputs["input_ids"] = input_ids
elif "x" in expected_inputs:
    ort_inputs["x"] = input_ids

# Map speaker ID
if "speaker_id" in expected_inputs:
    ort_inputs["speaker_id"] = np.array([speaker_id], dtype=np.int64)
elif "sid" in expected_inputs:
    ort_inputs["sid"] = np.array([speaker_id], dtype=np.int64)

# Map emotion ID
if "emotion_id" in expected_inputs:
    ort_inputs["emotion_id"] = np.array([emotion_id], dtype=np.int64)

# Map length scales (required if exported for Sherpa-ONNX)
possible_length_keys = ["x_length", "x_lengths", "x_vits_lengths"]
for length_key in possible_length_keys:
    if length_key in expected_inputs:
        # Pass the length of the token array
        ort_inputs[length_key] = np.array([input_ids.shape[1]], dtype=np.int64)
        break

for scale in ["noise_scale", "length_scale", "noise_scale_w"]:
    if scale in expected_inputs:
        ort_inputs[scale] = np.array(
            [0.667 if scale != "length_scale" else 1.0], dtype=np.float32
        )

# 3. Run Inference
print("Running ONNX inference...")
outputs = session.run(None, ort_inputs)

# 4. Extract and save audio
# The waveform is typically the first output returned by the graph
audio_array = outputs[0].squeeze()

# AI4Bharat VITS typically uses 16000Hz. If it sounds slow/deep, change to 22050.
sample_rate = 16000
sf.write("onnx_audio.wav", audio_array, sample_rate)

print(f"✅ Success! Audio saved to onnx_audio.wav with shape {audio_array.shape}")
