package com.central.gateway.filter;

import cn.hutool.core.util.StrUtil;
import eu.bitwalker.useragentutils.UserAgent;
import com.central.gateway.utils.ReactiveAddrUtil;
import com.central.log.monitor.PointUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 请求统计分析埋点过滤器
 *
 * @author zlt
 * @date 2019/10/7
 * <p>
 * Blog: https://zlt2000.gitee.io
 * Github: https://github.com/zlt2000
 */
@Component
public class RequestStatisticsFilter implements GlobalFilter, Ordered {


    private static final Log logger = LogFactory.getLog(RequestStatisticsFilter.class);
    private static final String START_TIME = "startTime";


    public RequestStatisticsFilter() {
        logger.info("Loaded GlobalFilter [Logging]");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Map<String, String> headers = request.getHeaders().toSingleValueMap();
        UserAgent userAgent = UserAgent.parseUserAgentString(headers.get("User-Agent"));
        //埋点
        PointUtil.debug("1", "request-statistics",
                "ip=" + ReactiveAddrUtil.getRemoteAddr(request)
                        + "&browser=" + getBrowser(userAgent.getBrowser().name())
                        + "&operatingSystem=" + getOperatingSystem(userAgent.getOperatingSystem().name()));


        String info = String.format("Method:{%s} Host:{%s} Path:{%s}",
                exchange.getRequest().getMethod().name(),
                exchange.getRequest().getURI().getHost(),
                exchange.getRequest().getURI().getPath());
        logger.info(info);

        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());
        return chain.filter(exchange).then( Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(START_TIME);
            if (startTime != null) {
                Long executeTime = (System.currentTimeMillis() - startTime);
                logger.info(exchange.getRequest().getURI().getRawPath() + " : " + executeTime + "ms");
            }
        }));


       // return chain.filter(exchange);
    }





    @Override
    public int getOrder() {
        return 0;
    }

    private String getBrowser(String browser) {
        if (StrUtil.isNotEmpty(browser)) {
            if (browser.contains("CHROME")) {
                return "CHROME";
            } else if (browser.contains("FIREFOX")) {
                return "FIREFOX";
            } else if (browser.contains("SAFARI")) {
                return "SAFARI";
            } else if (browser.contains("EDGE")) {
                return "EDGE";
            }
        }
        return browser;
    }

    private String getOperatingSystem(String operatingSystem) {
        if (StrUtil.isNotEmpty(operatingSystem)) {
            if (operatingSystem.contains("MAC_OS_X")) {
                return "MAC_OS_X";
            } else if (operatingSystem.contains("ANDROID")) {
                return "ANDROID";
            }
        }
        return operatingSystem;
    }
}
