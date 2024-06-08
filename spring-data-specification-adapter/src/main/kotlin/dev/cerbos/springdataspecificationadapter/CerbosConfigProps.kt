package dev.cerbos.springdataspecificationadapter

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("cerbos")
class CerbosConfigProps {
    lateinit var address: String
}