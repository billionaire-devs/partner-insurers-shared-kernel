package com.bamboo.assur.partnerinsurers.sharedkernel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SharedKernelApplication

fun main(args: Array<String>) {
	runApplication<SharedKernelApplication>(*args)
}
