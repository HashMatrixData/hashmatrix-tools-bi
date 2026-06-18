package io.hashmatrix.toolsbi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 数据工具（BI）分系统启动类。
 *
 * <p>工程基座：分层骨架（api/app/domain/infra）+ 多租户隔离取数 + Doris(OLAP) 连通，
 * 公共能力经 libs-java starter 复用。报表/自助分析/可视编排业务在本基座之上落地；
 * 报表设计器集成 DataEase（非自研），大屏与图表前端在 webui。
 */
@SpringBootApplication
public class ToolsBiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolsBiApplication.class, args);
    }
}
