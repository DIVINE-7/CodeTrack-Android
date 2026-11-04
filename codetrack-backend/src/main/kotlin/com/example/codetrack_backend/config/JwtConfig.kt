package com.example.codetrack_backend.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
class JwtConfig(

    @Value("\${app.jwt.secret}")
    private val secret: String
) {

    private fun secretKey(): SecretKey {
        return SecretKeySpec(
            secret.toByteArray(Charsets.UTF_8),
            "HmacSHA256"
        )
    }

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val jwk = OctetSequenceKey.Builder(secretKey())
            .algorithm(com.nimbusds.jose.JWSAlgorithm.HS256)
            .build()

        val jwkSource =
            ImmutableJWKSet<SecurityContext>(JWKSet(jwk))

        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder
            .withSecretKey(secretKey())
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }
}