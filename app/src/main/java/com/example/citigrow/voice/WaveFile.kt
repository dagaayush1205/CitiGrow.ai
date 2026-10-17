package com.example.citigrow.voice

import java.io.File
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WaveFile {
    fun write(file: File, samples: FloatArray, sampleRate: Int) {
        require(sampleRate in 8000..96000 && samples.isNotEmpty())
        require(samples.all { it.isFinite() }) { "Model returned invalid audio samples." }
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray()).putInt(36 + samples.size*2).put("WAVEfmt ".toByteArray())
            .putInt(16).putShort(1).putShort(1).putInt(sampleRate).putInt(sampleRate*2)
            .putShort(2).putShort(16).put("data".toByteArray()).putInt(samples.size*2)
        val temporary = File(file.parentFile, file.name + ".part")
        try {
            BufferedOutputStream(FileOutputStream(temporary)).use { output ->
                output.write(header.array())
                samples.forEach { sample ->
                    val pcm = (sample.coerceIn(-1f, 1f)*32767).toInt()
                    output.write(pcm and 255); output.write((pcm shr 8) and 255)
                }
            }
            check(temporary.renameTo(file)) { "Could not save generated speech." }
        } finally { temporary.delete() }
    }
}
