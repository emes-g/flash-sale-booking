package com.flashsale.booking.global.utils;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Spring Expression Language (SpEL) 파싱을 위한 커스텀 유틸리티 클래스.
 * 기본적으로 Spring은 커스텀 어노테이션에 작성된 SpEL을 자동으로 파싱하지 않으므로,
 * AOP 단계에서 이를 동적으로 평가(Evaluate)하기 위해 사용된다.
 */
public class CustomSpringELParser {

    /**
     * 메서드의 파라미터 정보와 SpEL 표현식을 조합하여 실제 동적인 키 값을 생성한다.
     *
     * @param parameterNames 메서드의 파라미터 이름 배열 (예: ["userId", "accommodationId"])
     * @param args           메서드 호출 시 전달된 실제 인자값 배열 (예: [1L, 5L])
     * @param key            어노테이션에 명시된 SpEL 표현식 (예: "'ACCOMMODATION:' + #accommodationId")
     * @return 파싱이 완료된 최종 락 키 문자열 (예: "ACCOMMODATION:5")
     */
    public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
        // 1. SpEL 파서 생성
        ExpressionParser parser = new SpelExpressionParser();

        // 2. 파라미터 이름과 값을 매핑하여 저장할 컨텍스트(단어장) 생성
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 3. 컨텍스트에 파라미터 이름과 실제 값을 매칭하여 세팅
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        // 4. 주어진 컨텍스트를 바탕으로 SpEL 표현식을 평가하여 결과값 반환
        return parser.parseExpression(key).getValue(context, Object.class);
    }
}