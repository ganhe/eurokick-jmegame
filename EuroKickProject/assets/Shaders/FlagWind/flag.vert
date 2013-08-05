#ifndef NUM_LIGHTS
  #define NUM_LIGHTS 1
#endif

uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform vec3 g_CameraPosition;

attribute vec3 inPosition;
attribute vec2 inTexCoord;

#ifdef SWAYING

uniform vec2 m_Wind;
uniform vec3 m_SwayData;
#endif

uniform float g_Time;

#ifdef VERTEX_LIGHTING

uniform vec4 g_LightPosition[NUM_LIGHTS];
uniform vec4 g_LightColor[NUM_LIGHTS];
uniform vec4 g_LightDirection[NUM_LIGHTS];

uniform vec4 g_AmbientLightColor;

varying vec3 diffuseLight;
varying vec3 ambientLight;

#endif

varying vec3 texCoord;

void main() {
    texCoord = vec3(inTexCoord,1.0);
    vec4 pos = vec4(inPosition,1.0);

    vec4 worldPos = g_WorldMatrix*pos;
    /*
    #ifdef VERTEX_LIGHTING
    diffuseLight = vec3(0.0,0.0,0.0);
    for(int i = 0; i < NUM_LIGHTS; i++){
        vec3 diffLight = vec3(0.0,0.0,0.0);
        float posLight = step(0.5, g_LightColor[i].w);
        vec3 lightVec = g_LightPosition[i].xyz * sign(posLight - 0.5) - (worldPos.xyz * posLight);
        float lDist = length(lightVec);

        float att = clamp(1.0 - g_LightPosition[i].w * lDist * posLight, 0.0, 1.0);
        lightVec = lightVec / vec3(lDist);
        //Spotlights
        float spotFallOff = 1.0;
        if(g_LightDirection[i].w != 0.0){
            vec3 spotdir = normalize(g_LightDirection[i].xyz);
            float curAngleCos = dot(-lightVec, spotdir);    
            float innerAngleCos = floor(g_LightDirection[i].w) * 0.001;
            float outerAngleCos = fract(g_LightDirection[i].w);
            float innerMinusOuter = innerAngleCos - outerAngleCos;
            spotFallOff = clamp((curAngleCos - outerAngleCos) / innerMinusOuter, 0.0, 1.0);
        }
        diffLight = g_LightColor[i].rgb*(att*spotFallOff);
        diffuseLight += diffLight;
    }
    ambientLight = g_AmbientLightColor.rgb;
    #endif
*/

    vec2 wind=vec2(1,1);
    float sway_x=1;
    float sway_y=1;

    float angle = (g_Time + pos.x * sway_y) * sway_x;
    //pos.xz += 0.1* wind * inTexCoord.y * sin(angle);
    gl_Position = gl_Position * sin(g_Time);
    gl_Position = g_WorldViewProjectionMatrix * pos;
}
