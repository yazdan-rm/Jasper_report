package ir.rbp.nabreport.dao.inbox.strategy.impl;

import ir.rbp.nabreport.common.constant.LiteralConstants;
import ir.rbp.nabreport.dao.inbox.strategy.AbstractUserRequestReportStrategy;
import ir.rbp.nabreport.dao.inbox.strategy.StrategyContext;
import ir.rbp.nabreport.model.viewmodel.inbox.UserRequestReportVM;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component(LiteralConstants.ALL_RECEIVED_REQUEST_IS_NOT_CHECKED_NAME)
// Define this component name to EN_UserRequestReportType
public class AllReceivedRequestIsNotCheckedReportStrategy extends AbstractUserRequestReportStrategy {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public ClassPathResource getJasperFile() {
        return new ClassPathResource("/report/inbox/35579.jrxml");
    }

    @Override
    public List<UserRequestReportVM> generateData(StrategyContext strategyContext) {

        String query = """
                SELECT
                    :DYNAMIC_GENERATED_SELECT_PORTION
                    COUNT(decode(ur.fk_pv_request_type_id, 34659, 1)) AS khademyarRazavi,
                    COUNT(decode(ur.fk_pv_request_type_id, 33834, 1)) AS membershipGroupTransfer,
                    COUNT(decode(ur.fk_pv_request_type_id, 33734, 1)) AS membershipTransfer,
                    COUNT(decode(ur.fk_pv_request_type_id, 34101, 1)) AS changeNationalCode,
                    COUNT(decode(ur.fk_pv_request_type_id, 34999, 1)) AS deductionDetails,
                    COUNT(CASE WHEN ur.fk_pv_request_type_id NOT IN(34659, 33834, 33734, 34101, 34999) THEN 1 END) AS otherRequests
                FROM
                    org.org_parent_titles_matv org
                    INNER JOIN usr$request_member um ON um.user_org_id = org.org_id
                    INNER JOIN usr$request       ur ON ur.id = um.fk_request_id
                WHERE
                    um.fk_pv_response_status_id IS NULL
                    AND um.fk_pv_member_type_id <> 33881
                    AND :DYNAMIC_GENERATED_CONDITION_PORTION
                GROUP BY
                        :DYNAMIC_GENERATED_GROUP_BY_PORTION
                ORDER BY
                        :DYNAMIC_GENERATED_ORDER_BY_PORTION
                """;

        Map<String, Object> filters = new HashMap<>();
        String columnName = getColumnNameByOrgTypeLayerNum(strategyContext.getLayerNumOfSelectedOrgIds());
        query = query
                    .replace(":DYNAMIC_GENERATED_SELECT_PORTION", generateGroupedColumnForSelect(strategyContext))
                    .replace(":DYNAMIC_GENERATED_CONDITION_PORTION", generateWhereStatement(strategyContext, columnName, filters))
                    .replace(":DYNAMIC_GENERATED_GROUP_BY_PORTION", generateGroupByStatement(strategyContext))
                    .replace(":DYNAMIC_GENERATED_ORDER_BY_PORTION", generateGroupByStatement(strategyContext));

        return namedParameterJdbcTemplate.query(query, filters, new BeanPropertyRowMapper<>(UserRequestReportVM.class));
    }
}
