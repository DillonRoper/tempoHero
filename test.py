import aubio
import pyaudio
import wave


chunk = 1024  # Record in chunks of 1024 samples
sample_format = pyaudio.paInt16  # 16 bits per sample
channels = 1  # Mono
fs = 44100  # Record at 44100 samples per second
seconds = 5
filename = "output.wav"

p = pyaudio.PyAudio()  # Create an interface to PortAudio

print('Recording')

stream = p.open(format=sample_format,
                channels=channels,
                rate=fs,
                input=True,
                frames_per_buffer=chunk)

frames = []  # Initialize array to store frames

# Store data in chunks for 5 seconds
for i in range(0, int(fs / chunk * seconds)):
    data = stream.read(chunk)
    frames.append(data)

# Stop and close the stream 
stream.stop_stream()
stream.close()
# Terminate the PortAudio interface
p.terminate()

print('Finished recording')

# Save the recorded data as a WAV file
wf = wave.open(filename, 'wb')
wf.setnchannels(channels)
wf.setsampwidth(p.get_sample_size(sample_format))
wf.setframerate(fs)
wf.writeframes(b''.join(frames))
wf.close()
#--------------------------------------------------------------------------------------------------------
def get_tempo(audio_path):
    # Create a tempo detection object
    win_s = 1024                 # FFT window size
    hop_s = win_s // 2          # hop size
    samplerate = 0              # aubio will auto-detect

    # Load audio source
    source = aubio.source(audio_path, samplerate, hop_s)
    samplerate = source.samplerate

    tempo_o = aubio.tempo("default", win_s, hop_s, samplerate)

    beats = []
    total_frames = 0

    while True:
        samples, read = source()
        is_beat = tempo_o(samples)
        if is_beat:
            this_beat = tempo_o.get_last_s()
            beats.append(this_beat)
        total_frames += read
        if read < hop_s:
            break

    if len(beats) > 1:
        # Calculate average BPM from beat intervals
        intervals = [j - i for i, j in zip(beats[:-1], beats[1:])]
        bpm = 60.0 / (sum(intervals) / len(intervals))
        return round(bpm, 2)
    else:
        return None

# Example usage:
#"C:\\Users\\talal\\Downloads\\Taylor Swift - Blank Space (Audio).wav"
#"C:\\Users\\talal\Downloads\\DOVER QUARTET - Dvorak： ＂American＂： 4. Finale.wav"
file_path = "output.wav"  # Must be WAV (mono, 44.1kHz recommended)
bpm = get_tempo(file_path)
print(f"Estimated BPM: {bpm}" if bpm else "Not enough beats detected.")

