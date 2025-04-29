import aubio

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
file_path = "C:\\Users\\talal\Downloads\\Taylor Swift - Blank Space (Audio).wav"  # Must be WAV (mono, 44.1kHz recommended)
bpm = get_tempo(file_path)
print(f"Estimated BPM: {bpm}" if bpm else "Not enough beats detected.")

