from transformers import AutoModel, AutoTokenizer, AutoConfig
import torch
import soundfile as sf

config = AutoConfig.from_pretrained("ai4bharat/vits_rasa_13", trust_remote_code=True)

config.pad_token_id = 0

model = AutoModel.from_pretrained(
    "ai4bharat/vits_rasa_13", config=config, trust_remote_code=True
)
tokenizer = AutoTokenizer.from_pretrained(
    "ai4bharat/vits_rasa_13", trust_remote_code=True
)

text = "मेरा नाम आयुष है। यह एक टेस्ट है। क्या ऑडियो ठीक है?" 
speaker_id = 16  # PAN_M
style_id = 0  # ALEXA

inputs = tokenizer(text=text, return_tensors="pt")
with torch.no_grad():
    outputs = model(inputs["input_ids"], speaker_id=speaker_id, emotion_id=style_id)

# 4. Extract audio, move to CPU, and convert to NumPy
audio_array = outputs.waveform.squeeze().cpu().numpy()

# 5. Save the output
sf.write("audio.wav", audio_array, model.config.sampling_rate)
print("Success! Output shape:", audio_array.shape)
