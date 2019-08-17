#version 150

#define SAMPLER0 sampler2D // sampler2D, sampler3D, samplerCube
#define SAMPLER1 sampler2D // sampler2D, sampler3D, samplerCube
#define SAMPLER2 sampler2D // sampler2D, sampler3D, samplerCube
#define SAMPLER3 sampler2D // sampler2D, sampler3D, samplerCube

uniform SAMPLER0 iChannel0; // image/buffer/sound    Sampler for input textures 0
uniform SAMPLER1 iChannel1; // image/buffer/sound    Sampler for input textures 1
uniform SAMPLER2 iChannel2; // image/buffer/sound    Sampler for input textures 2
uniform SAMPLER3 iChannel3; // image/buffer/sound    Sampler for input textures 3

uniform vec3  iResolution;           // image/buffer          The viewport resolution (z is pixel aspect ratio, usually 1.0)
uniform float iTime;                 // image/sound/buffer    Current time in seconds
uniform float iTimeDelta;            // image/buffer          Time it takes to render a frame, in seconds
uniform int   iFrame;                // image/buffer          Current frame
uniform float iFrameRate;            // image/buffer          Number of frames rendered per second
uniform vec4  iMouse;                // image/buffer          xy = current pixel coords (if LMB is down). zw = click pixel
uniform vec4  iDate;                 // image/buffer/sound    Year, month, day, time in seconds in .xyzw
uniform float iSampleRate;           // image/buffer/sound    The sound sample rate (typically 44100)
uniform float iChannelTime[4];       // image/buffer          Time for channel (if video or sound), in seconds
uniform vec3  iChannelResolution[4]; // image/buffer/sound    Input texture resolution for each channel

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
  vec2 c = fragCoord.xy / iResolution.xy;
  float width = iMouse.x;

  //c.x *= 10.0;  // Scale it in the X direction for a better visual fit.
  //c.y -= 0.2f;
  //c.y = 1.0f - c.y;
  float yOffset = iMouse.y;
  c.y -= yOffset;
  c.y *= iMouse.z * 5.0;

  fragColor = vec4(1.0, 1.0, 1.0, 1.0);
  float mag = texture(iChannel0, vec2(0.2*c.x, 0.2*c.y)).r;
  float adjustedY = c.y; // c.y * 1.0 / (1.0 + c.x * 2.0);
  
  if ( abs(adjustedY - mag) > width) {
    fragColor = vec4(0.0, 0.0, 0.0, 0.0);
  } else {
    fragColor = vec4(1.0, 1.0, 1.0, 1.0);
  }
}  
