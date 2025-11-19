package com.bamboo.assur.partnerinsurers.sharedkernel.presentation

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import kotlinx.datetime.Clock

/**
 * Simple servlet filter that stores the request start timestamp as a request
 * attribute so that downstream components can compute processing duration.
 */
class SharedKernelRequestTimingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        request.setAttribute(REQUEST_START_ATTRIBUTE, Clock.System.now())
        filterChain.doFilter(request, response)
    }

    companion object {
        /** Request attribute used to store the request start [Instant]. */
        const val REQUEST_START_ATTRIBUTE: String =
            "com.bamboo.assur.partnerinsurers.sharedkernel.presentation.REQUEST_START"
    }
}

