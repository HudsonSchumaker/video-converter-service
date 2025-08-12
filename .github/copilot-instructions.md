# Copilot Instructions

<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

## Project Context
This is a Spring Boot service for file format conversion using FFmpeg. The service provides REST API endpoints to:
- Upload files in various formats
- Convert files between different formats (video, audio, image)
- Download converted files
- Check conversion status

## Architecture Guidelines
- Use Spring Boot best practices with proper error handling
- Implement async processing for file conversions
- Use proper validation for file types and sizes
- Follow RESTful API design principles
- Include comprehensive logging and monitoring

## Key Technologies
- Spring Boot 3.x with Java 17
- FFmpeg for file conversion
- Maven for dependency management
- Spring Web for REST endpoints
- Spring Validation for input validation
- Spring Actuator for monitoring

## Code Style
- Use constructor injection for dependencies
- Implement proper exception handling with custom exceptions
- Use DTOs for API requests/responses
- Follow Spring Boot naming conventions
- Include proper JavaDoc documentation
