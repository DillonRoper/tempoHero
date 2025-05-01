import aubio
import pyaudio
import numpy as np
import keyboard

# Audio stream config
buffer_size = 1024
sample_format = pyaudio.paInt16
channels = 1
sample_rate = 44100
chunk_duration = buffer_size / sample_rate  # seconds
seconds_per_segment = 10
frames_per_segment = int(sample_rate / buffer_size * seconds_per_segment)

# Aubio config
window_size = 1024
hop_size = window_size // 2

# Initialize PyAudio and Aubio
audio_interface = pyaudio.PyAudio()
stream = audio_interface.open(format=sample_format,
                              channels=channels,
                              rate=sample_rate,
                              input=True,
                              frames_per_buffer=buffer_size)

tempo_detector = aubio.tempo("default", window_size, hop_size, sample_rate)
bpm_log = []

print("Listening... Press SPACE to stop.")

try:
    while True:
        segment_frames = []
        print(f"Recording {seconds_per_segment}-second segment...")

        for x  in range(frames_per_segment):
            if keyboard.is_pressed("space"):
                raise KeyboardInterrupt
            data = stream.read(buffer_size, exception_on_overflow=False)
            samples = np.frombuffer(data, dtype=np.int16).astype(np.float32) / 32768.0
            segment_frames.append(samples)

        combined_samples = np.concatenate(segment_frames)
        beat_times = []

        for i in range(0, len(combined_samples), hop_size):
            chunk = combined_samples[i:i + hop_size]
            if len(chunk) < hop_size:
                break
            if tempo_detector(chunk):
                beat_times.append(tempo_detector.get_last_s())

        if len(beat_times) > 1:
            intervals = [j - i for i, j in zip(beat_times[:-1], beat_times[1:])]
            bpm = 60.0 / (sum(intervals) / len(intervals))
            bpm = round(bpm, 2)
            bpm_log.append(bpm)
            print(f"Estimated BPM: {bpm}")
        else:
            print("Not enough beats detected.")
except KeyboardInterrupt:
    print("\nSpacebar pressed. Stopping...")

finally:
    stream.stop_stream()
    stream.close()
    audio_interface.terminate()
    print("All BPMs recorded:", bpm_log)