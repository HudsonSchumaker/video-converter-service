# File Conversion Service

A Spring Boot service that provides file format conversion using FFmpeg. The service off## ⚙️ Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server configuration
server.port=8080

# File upload limits
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Application settings
app.ffmpeg.path=ffmpeg
app.upload.dir=./uploads
app.output.dir=./output
app.max.file.size=100MB

# GPU acceleration settings
app.ffmpeg.gpu.enabled=true
app.ffmpeg.gpu.auto-detect=true
app.ffmpeg.gpu.preferred=auto
```

### 🎮 GPU Acceleration

The service automatically detects and uses available GPU acceleration:

- **macOS**: Apple VideoToolbox (built-in)
- **NVIDIA**: NVENC (requires NVIDIA GPU with hardware encoding support)
- **AMD**: AMF (requires AMD GPU with AMF support)
- **Intel**: Quick Sync Video (requires Intel CPU/GPU with Quick Sync)

**Benefits of GPU acceleration:**
- 🚀 **3-10x faster** video encoding
- 💻 **Lower CPU usage** during conversion
- 🔋 **Better power efficiency** on laptops
- 🎯 **Optimized quality settings** for each GPU type

To disable GPU acceleration, set `app.ffmpeg.gpu.enabled=false`nts for uploading files, converting them between different formats, and downloading the converted files with proper async processing.

## ✨ Recent Updates

- **GPU Acceleration Support**: Automatic detection and use of hardware acceleration
  - **Apple VideoToolbox** (macOS) 
  - **NVIDIA NVENC** (NVIDIA GPUs)
  - **AMD AMF** (AMD GPUs)
  - **Intel Quick Sync** (Intel GPUs)
- **Fixed Async Processing**: Conversion now runs in dedicated thread pool for true non-blocking operation
- **Enhanced Status Polling**: Real-time status updates with faster polling intervals
- **Improved Error Handling**: Better error reporting and validation
- **Thread Pool Configuration**: Dedicated executor for conversion tasks

## 🚀 Features

- **GPU Hardware Acceleration**: Automatic detection and use of hardware encoders for faster conversion
- **True Async File Conversion**: Non-blocking conversion using dedicated thread pool
- **Multiple Format Support**: Video (MP4, AVI, MOV, MKV), Audio (MP3, WAV, FLAC, AAC), Image (JPG, PNG, GIF, WebP)
- **Quality Control**: Low, medium, and high quality presets optimized for each encoder
- **Custom Parameters**: Control resolution, bitrate, and other conversion settings
- **Real-time Job Tracking**: Live status updates with unique job IDs
- **File Download**: Direct download of converted files
- **Health Monitoring**: Check service, FFmpeg, and GPU acceleration availability
- **Web Interface**: Simple HTML interface for testing with GPU status display
- **Comprehensive Error Handling**: Validation and error reporting
- **Custom Parameters**: Support for custom width, height, and bitrate settings
- **Download**: Download converted files through the API
- **Health Check**: Service health monitoring with FFmpeg availability check
- **Web Interface**: Simple HTML interface for testing the API

## Prerequisites

- Java 17 or higher
- Maven 3.6+ 
- FFmpeg installed and available in PATH

### Installing FFmpeg

**macOS (using Homebrew):**
```bash
brew install ffmpeg
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install ffmpeg
```

**Windows:**
Download from [https://ffmpeg.org/download.html](https://ffmpeg.org/download.html) and add to PATH.

## Getting Started

### 1. Clone and Build

```bash
# Navigate to project directory
cd service

# Build the project
./mvnw clean compile
```

### 2. Run the Application

```bash
./mvnw spring-boot:run
```

The service will start on `http://localhost:8080`

### 3. Test the Service

Open your browser and go to `http://localhost:8080` to access the web interface, or use the API endpoints directly.

## API Endpoints

### Convert File
```http
POST /api/convert
Content-Type: multipart/form-data

Parameters:
- file: The file to convert (required)
- targetFormat: Target format (required) - mp4, avi, mov, mkv, mp3, wav, flac, aac, jpg, png, gif, webp
- quality: Quality setting (optional) - low, medium, high (default: medium)
- width: Target width in pixels (optional)
- height: Target height in pixels (optional)
- bitrate: Target bitrate in kbps (optional)
```

### Check Status
```http
GET /api/status/{jobId}
```

### Download File
```http
GET /api/files/download/{jobId}
```

### Health Check
```http
GET /api/health
```

### Supported Formats
```http
GET /api/formats
```

## Configuration

The service can be configured through `application.properties`:

```properties
# Server configuration
server.port=8080

# File upload configuration
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Application configuration
app.ffmpeg.path=ffmpeg
app.upload.dir=./uploads
app.output.dir=./output
app.max.file.size=100MB
```

## Usage Examples

### Using cURL

**Convert a video to MP4:**
```bash
curl -X POST -F "file=@input.avi" -F "targetFormat=mp4" -F "quality=high" http://localhost:8080/api/convert
```

**Check conversion status:**
```bash
curl http://localhost:8080/api/status/{jobId}
```

**Download converted file:**
```bash
curl -O http://localhost:8080/api/files/download/{jobId}
```

### Using the Web Interface

1. Open `http://localhost:8080` in your browser
2. Select a file to upload
3. Choose the target format and quality settings
4. Click "Convert File"
5. Monitor the progress and download when complete

## Response Examples

**Conversion Started:**
```json
{
  "jobId": "123e4567-e89b-12d3-a456-426614174000",
  "status": "PENDING",
  "originalFileName": "video.avi",
  "originalFormat": "avi",
  "targetFormat": "mp4",
  "originalFileSize": 52428800,
  "createdAt": "2025-07-17T10:30:00"
}
```

**Status Check:**
```json
{
  "jobId": "123e4567-e89b-12d3-a456-426614174000",
  "status": "COMPLETED",
  "originalFileName": "video.avi",
  "convertedFileName": "video_converted.mp4",
  "originalFormat": "avi",
  "targetFormat": "mp4",
  "createdAt": "2025-07-17T10:30:00",
  "completedAt": "2025-07-17T10:32:15",
  "downloadUrl": "/api/files/download/123e4567-e89b-12d3-a456-426614174000",
  "originalFileSize": 52428800,
  "convertedFileSize": 48234567
}
```

## Error Handling

The service provides comprehensive error handling:

- **Validation errors**: Invalid file types, missing parameters
- **File size errors**: Files exceeding maximum size
- **Conversion errors**: FFmpeg processing failures
- **System errors**: FFmpeg not available, disk space issues

## Monitoring

- **Health endpoint**: `/api/health` - Check service and FFmpeg status
- **Actuator endpoints**: `/actuator/health`, `/actuator/metrics`
- **Logging**: Configurable logging levels for debugging

## Security Considerations

- File size limits to prevent abuse
- Input validation on all parameters
- Temporary file cleanup (implement as needed)
- Consider authentication for production use

## Limitations

- Files are processed synchronously by FFmpeg
- Converted files are stored on the server filesystem
- No database persistence (jobs are stored in memory)
- No automatic cleanup of old files

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
