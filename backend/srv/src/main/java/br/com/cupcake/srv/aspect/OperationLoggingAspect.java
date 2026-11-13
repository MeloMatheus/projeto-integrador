package br.com.cupcake.srv.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationLoggingAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationLoggingAspect.class);

    @Around("execution(public * br.com.cupcake.srv.controller..*(..))")
    public Object logControllerOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return logOperation(joinPoint, "controller");
    }

    @Around("execution(public * br.com.cupcake.srv.service..*(..))")
    public Object logServiceOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return logOperation(joinPoint, "service");
    }

    @Around("bean(*Repository)")
    public Object logRepositoryOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return logOperation(joinPoint, "persistência");
    }

    private Object logOperation(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            LOGGER.info("Operação concluída com sucesso em {}.{}#{}()", layer, className, methodName);
            return result;
        } catch (Throwable exception) {
            LOGGER.error("Falha na operação em {}.{}#{}()", layer, className, methodName, exception);
            throw exception;
        }
    }
}
