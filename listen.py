# Talal Kheiry  
# 05/06/2025  
# Live BPM Detection Tool - with socket communication to Java frontend

import aubio
import pyaudio
import numpy
import keyboard
import socket
import json

# ====== Config ======

# Audio input settings
BUFFER_SIZE = 1024
SAMPLE_FORMAT = pyaudio.paInt16
CHANNELS = 1
SAMPLE_RATE = 44100
SEGMENT_DURATION = 5
FRAMES_PER_SEGMENT = int(SAMPLE_RATE / BUFFER_SIZE * SEGMENT_DURATION)

# Aubio tempo detection
WINDOW_SIZE = 1024
HOP_SIZE = WINDOW_SIZE // 2

# Socket settings
HOST = 'localhost'
PORT = 12345

# ====== Initialization ======  

# Start socket server
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind((HOST, PORT))
server_socket.listen(1)
print(f"[Socket] Waiting for frontend connection on {HOST}:{PORT}...")
conn, addr = server_socket.accept()
print(f"[Socket] Connected to {addr}")

# Start audio input stream
audio_interface = pyaudio.PyAudio()
stream = audio_interface.open(format=SAMPLE_FORMAT,
                              channels=CHANNELS,
                              rate=SAMPLE_RATE,
                              input=True,
                              frames_per_buffer=BUFFER_SIZE)

# Set up tempo detector
tempo_detector = aubio.tempo("default", WINDOW_SIZE, HOP_SIZE, SAMPLE_RATE)
bpm_log = []

print("Listening... Press SPACE to stop.\n")

# ====== BPM Detection Loop ======

try:
    while True:
        print(f"Recording {SEGMENT_DURATION}-second segment...")

        segment_samples = []
        for _ in range(FRAMES_PER_SEGMENT):
            if keyboard.is_pressed("space"):
                raise KeyboardInterrupt
            data = stream.read(BUFFER_SIZE, exception_on_overflow=False)
            samples = numpy.frombuffer(data, dtype=numpy.int16).astype(numpy.float32) / 32768.0
            segment_samples.append(samples)

        combined_samples = numpy.concatenate(segment_samples)
        beat_times = []

        for i in range(0, len(combined_samples), HOP_SIZE):
            chunk = combined_samples[i:i + HOP_SIZE]
            if len(chunk) < HOP_SIZE:
                break
            if tempo_detector(chunk):
                beat_times.append(tempo_detector.get_last_s())

        if len(beat_times) > 1:
            intervals = [j - i for i, j in zip(beat_times[:-1], beat_times[1:])]
            avg_interval = sum(intervals) / len(intervals)
            bpm = round(60.0 / avg_interval, 2)
            bpm_log.append(bpm)
            print(f"Estimated BPM: {bpm}\n")

            # Send bpm_log to frontend
            msg = json.dumps(bpm) + "\n"  # add newline for framing
            conn.sendall(msg.encode())
        else:
            print("Not enough beats detected.\n")

except KeyboardInterrupt:
    print("\nSpacebar pressed. Terminating BPM detection...")

finally:
    stream.stop_stream()
    stream.close()
    audio_interface.terminate()
    conn.close()
    server_socket.close()
    print("Session ended. BPMs recorded:", bpm_log)