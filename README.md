# Android Media Library

## Download
Gradle:
```
implementation 'com.kimyi:android-media:0.0.4'
```

## Feature
### Record
- AudioRecorder: record audio data from mic or other source.
- VideoRecorder: record video data from screen.
- MediaRecordHelper/ScreenRecorder: record audio and video, encode and mux them to video file.

### Codec
- BaseCodec: base codec wrap `MediaCodec`.
- SyncCodec: synchronous codec extends `BaseCodec`.
- AsyncCodec: asynchronous codec extends `BaseCodec`.   

> Extending `SyncCodec` or `AsyncCodec` to implement encoder/decoder for audio/video in synchronous/asynchronous mode. 
> E.g. `AudioEncoder`/`AudioEncoder2`, `VideoEncoder`/`VideoEncoder2`.

### Encode
- AudioEncoder/AudioEncoder2: encode audio data in synchronous/asynchronous mode.
- VideoEncoder/VideoEncoder2: encode video data in synchronous/asynchronous mode.

### Mux
- MediaMuxerHelper: mux encoded audio and video data to video file.