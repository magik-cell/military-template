package ua.edu.viti.military.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter для додавання Correlation ID до всіх запитів
 * Correlation ID дозволяє відстежувати один запит через всю систему
 */
@Component
public class CorrelationIdFilter implements Filter {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // 1. Отримати або згенерувати Correlation ID
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }
            
            // 2. Встановити в MDC (Mapped Diagnostic Context)
            // MDC - ThreadLocal контекст, який автоматично додається до всіх логів
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            
            // 3. Додати в response header (клієнт може використати для tracing)
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            // 4. Продовжити обробку запиту
            chain.doFilter(request, response);
            
        } finally {
            // 5. Очистити MDC після обробки (важливо для thread pools)
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}
