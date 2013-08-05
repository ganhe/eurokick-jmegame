varying vec3 texCoord;

#ifdef VERTEX_LIGHTING
varying vec3 diffuseLight;
varying vec3 ambientLight;
#endif

uniform sampler2D m_ColorMap;



void main() {

    vec4 outColor = texture2D(m_ColorMap, texCoord.xy);
    
    #ifdef VERTEX_LIGHTING    
    //outColor.rgb *= (diffuseLight + ambientLight);
    #endif

    gl_FragColor = outColor;
}

