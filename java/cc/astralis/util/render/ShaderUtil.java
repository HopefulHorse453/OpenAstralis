package cc.astralis.util.render;

import cc.astralis.Astralis;

import java.awt.*;

// this is aids.
public class ShaderUtil {
    public static String getCustomShader() {
        Color color = Astralis.getInstance().getFirstColor();

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float glowIntensity = 2;

        return """
            #version 150
            uniform sampler2D InSampler;
            in vec2 texCoord;
            in vec2 sampleStep;
            out vec4 fragColor;

            // Improved Gaussian function with normalization
            float gaussian(float x, float sigma) {
                return exp(-(x * x) / (2.0 * sigma * sigma)) / (sqrt(2.0 * 3.14159265359) * sigma);
            }

            // Two-pass blur for better performance and quality
            vec4 horizontalBlur(float radius, float sigma) {
                vec4 result = vec4(0.0);
                float totalWeight = 0.0;
                
                for (float x = -radius; x <= radius; x += 1.0) {
                    float weight = gaussian(x, sigma);
                    vec2 sampleCoord = texCoord + vec2(sampleStep.x * x, 0.0);
                    result += texture(InSampler, sampleCoord) * weight;
                    totalWeight += weight;
                }
                
                return result / totalWeight;
            }

            vec4 verticalBlur(float radius, float sigma, vec4 horizontalResult) {
                vec4 result = vec4(0.0);
                float totalWeight = 0.0;
                
                for (float y = -radius; y <= radius; y += 1.0) {
                    float weight = gaussian(y, sigma);
                    vec2 sampleCoord = texCoord + vec2(0.0, sampleStep.y * y);
                    result += texture(InSampler, sampleCoord) * weight;
                    totalWeight += weight;
                }
                
                // Blend between horizontal and vertical passes for smoother results
                return mix(result / totalWeight, horizontalResult, 0.5);
            }

            void main() {
                float radius = 8.0;  // Increased radius for better glow effect
                float sigma = radius / 2.0;  // Auto-calculated sigma based on radius
                
                // Perform two-pass blur
                vec4 horizontalBlurred = horizontalBlur(radius, sigma);
                vec4 blurred = verticalBlur(radius, sigma, horizontalBlurred);
                
                // Color and intensity
                vec3 glowColor = vec3(%s, %s, %s);
                float glowStrength = blurred.a * %s;
                
                // Optional: Add some edge enhancement
                vec4 original = texture(InSampler, texCoord);
                float edgeFactor = smoothstep(0.3, 0.7, original.a);
                
                // Combine glow with original (optional)
                fragColor = vec4(glowColor * glowStrength, glowStrength);
                
                // Alternative: Additive blending for stronger glow
                // fragColor = vec4(glowColor * glowStrength, glowStrength) + original;
            }
        """.formatted(r, g, b, glowIntensity);
    }
}