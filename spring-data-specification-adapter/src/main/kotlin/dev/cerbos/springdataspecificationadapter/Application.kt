package dev.cerbos.springdataspecificationadapter


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties
@SpringBootApplication
class Application

fun main( args: Array<String>){
    runApplication<Application>(*args)
}