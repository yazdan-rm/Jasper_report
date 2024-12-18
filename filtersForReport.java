package ir.rbp.nabreport.dao.absorption.impl;

import ir.rbp.nabcore.dao.impl.ReadOnlyGenericRepository;
import ir.rbp.nabcore.model.domainmodel.BaseEntity;
import ir.rbp.nabcore.model.formview.BaseEntityFV;
import ir.rbp.nabreport.common.enumeration.pvs.EN_LayerNum;
import ir.rbp.nabreport.common.enumeration.pvs.EN_OrgTypeLayerNum;
import ir.rbp.nabreport.model.viewmodel.ReportVM;
import org.apache.commons.collections.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public abstract class CommonAbsorptionDao extends ReadOnlyGenericRepository<BaseEntity, BaseEntityFV> {
    static final String ARCHIVE_STATUS_FILTER = " mat.MEM_IS_ARCHIVED = 0 and mat.MEM_ARCHIVE_TYPE_ID is null ";
    static final String CORTEX_FILTER = " mat.mem_org_cortex_id not in (154009, 154026) ";
    static final String MAT_MEM_ORG_ZONE = "mat.MEM_ORG_ZONE";
    static final String ORG_IS_NOT_DELETED = " org.is_deleted=0 and \n";

    static final String PRE_NAME_OF_ABSORPTION_ORG_COLUMN = " mat.mem_";
    static final String DEFAULT_FIRST_MEMBERSHIP_FILTERS = " mat.mem_is_first_mem = 1 and mat.mem_is_final = 1 \n and ";
    static final String DEFAULT_LAST_MEMBERSHIP_FILTER = " mat.mem_is_last_mem = 1 and mat.mem_is_archived = 0 and mat.mem_archive_type_id is null \n and ";
    static final String MAIN_MEMBERSHIP_TYPE_FILTER = " mat.MEM_TYPE_ID in (143,144,145) \n and ";
    static final String EXCLUDE_SPECIFIC_CORTEX_IDS_CRX = " crx.id not in (154009, 154026) \n ";
    static final String CORTEX_IS_DELETED = " crx.is_deleted=0 and \n";
    static final String EXCLUDE_SPECIFIC_CORTEX_IDS_MAT = " mat.mem_org_cortex_id not in (154009, 154026) \n";
    static final String EXCLUDE_MEMBERSHIP_TYPE_ID_MAT = " mat.mem_type_id not in (142) \n";
    static final String WHERE_TOTAL_MEMBERSHIP_COLUMNS = "(commonCountMale + commonCountFemale + activeCountMale + activeCountFemale) ";
    static final String WHERE_TOTAL_ORGANIZING_COLUMNS = "(commonOrganizedCountMale + commonOrganizedCountFemale + activeOrganizedCountMale + activeOrganizedCountFemale) ";
    static final String[] ORG_LEVEL_COLUMN_TITLE_NAMES = {" min(org.ostan_title) ostanTitle", " min(org.nahiyeh_title) nahiyehTitle", " min(org.hozeh_title) hozehTitle", " min(org.paygah_title) paygahTitle"};
    static final String[] ORG_LEVEL_COLUMN_ID_NAMES = {" org.ostan_id", " org.nahiyeh_id", " org.hozeh_id", " org.paygah_id"};

    String completeProvinceQuery(ReportVM reportVM, String query, Map<String, Object> filtersParam, boolean isSetadAndOstanInSameCase) {
        String orgLayerColumnNameForFilter = getColumnNameByOrgTypeLayerNum(reportVM.getLayerNumOfSelectedOrgIds(), " org.");
        String columnNameIsNotNull = generateColumnNameIsNotNull(reportVM, isSetadAndOstanInSameCase);
        String generateGroupByStatement = generateGroupByStatement(reportVM, isSetadAndOstanInSameCase);

        return String.format(query,
                generateGroupedColumnForSelect(reportVM, isSetadAndOstanInSameCase),
                generateWhereStatement(reportVM, filtersParam),
                generateOuterWhereStatement(columnNameIsNotNull, orgLayerColumnNameForFilter, reportVM.getIsEnable()),
                generateGroupByStatement);
    }

    String completeProvinceQuery4ShowOrganizationColumn(ReportVM reportVM, String query, Map<String, Object> filtersParam, boolean isSetadAndOstanInSameCase) {
        String orgLayerColumnNameForFilter = getColumnNameByOrgTypeLayerNum(reportVM.getLayerNumOfSelectedOrgIds(), " org.");
        String columnNameIsNotNull = generateColumnNameIsNotNull(reportVM, isSetadAndOstanInSameCase);
        String generateGroupByStatement = generateGroupByStatement(reportVM, isSetadAndOstanInSameCase);

        String queryTemp = generateOuterWhereStatement(columnNameIsNotNull, orgLayerColumnNameForFilter, reportVM.getIsEnable());
        queryTemp = !queryTemp.equals("") ? " and "+queryTemp: queryTemp;
        String query3 =  queryTemp ;
        queryTemp = generateOuterWhereStatementCortex(reportVM.getCortexIds());
        queryTemp = !queryTemp.equals("") ? " and "+queryTemp: queryTemp;
        query3 =  query3 + " " + queryTemp ;

        String query1 = generateWhereStatement(reportVM, filtersParam);

        String query2 = generateOuterWhereStatementCortex(reportVM.getCortexIds()).replaceAll("crx.id","org.cortex_id");
        query2 = !query2.equals("") ? " and "+query2: query2;
        String query5 = generateGroupedColumnForSelect(reportVM, isSetadAndOstanInSameCase).replaceAll("parentOrgTitle,"," ") ;
        String query4 = !generateGroupByStatement.equals("") ? generateGroupByStatement + "," : generateGroupByStatement;

        return String.format(query, query1,query2,query3,query4,query5);
    }

    String completeProvinceQuerySeparatelyCortexSex(ReportVM reportVM, String query, Map<String, Object> filtersParam, boolean isSetadAndOstanInSameCase) {
        String orgLayerColumnNameForFilter = getColumnNameByOrgTypeLayerNum(reportVM.getLayerNumOfSelectedOrgIds(), " org.");
        String columnNameIsNotNull = generateColumnNameIsNotNull(reportVM, isSetadAndOstanInSameCase);
        String generateGroupByStatement = generateGroupByStatement(reportVM, isSetadAndOstanInSameCase);
        String querySexTypeAndCoortex = " where 1=1 ";
        if(reportVM.getPvSexTypeIds() != null && !reportVM.getPvSexTypeIds().isEmpty())
            querySexTypeAndCoortex = querySexTypeAndCoortex + " and crx.sex_Id in (:psnSexIds) ";

        if(reportVM.getCortexIds() != null && !reportVM.getCortexIds().isEmpty())
            querySexTypeAndCoortex = querySexTypeAndCoortex+ " and crx.cortex_Id in (:cortexIds) ";

        return String.format(query,
                generateGroupedColumnForSelect(reportVM, isSetadAndOstanInSameCase),
                generateWhereStatement(reportVM, filtersParam),
                generateOuterWhereStatement(columnNameIsNotNull, orgLayerColumnNameForFilter, reportVM.getIsEnable()),
                generateGroupByStatement,
                querySexTypeAndCoortex);
    }

    String completeProvinceQuerySeparatelyCortexMemType(ReportVM reportVM, String query, Map<String, Object> filtersParam, boolean isSetadAndOstanInSameCase) {
        String orgLayerColumnNameForFilter = getColumnNameByOrgTypeLayerNum(reportVM.getLayerNumOfSelectedOrgIds(), " org.");
        String columnNameIsNotNull = generateColumnNameIsNotNull(reportVM, isSetadAndOstanInSameCase);
        String generateGroupByStatement = generateGroupByStatement(reportVM, isSetadAndOstanInSameCase);
        String queryMemTypeAndCoortex = " ";

        if(reportVM.getCortexIds() != null && !reportVM.getCortexIds().isEmpty())
            queryMemTypeAndCoortex = " where crx.cortex_Id in (:cortexIds) ";

        return String.format(query,
                generateGroupedColumnForSelect(reportVM, isSetadAndOstanInSameCase),
                generateWhereStatement(reportVM, filtersParam),
                generateOuterWhereStatement(columnNameIsNotNull, orgLayerColumnNameForFilter, reportVM.getIsEnable()),
                generateGroupByStatement,
                queryMemTypeAndCoortex);
    }
    String completeProvinceQueryIncreaseDecrease(ReportVM reportVM, String query, Map<String, Object> filtersParam, boolean isSetadAndOstanInSameCase) {

        String queryForDate = getQueryForDate(reportVM, filtersParam);
        String orgLayerColumnNameForFilter = getColumnNameByOrgTypeLayerNum(reportVM.getLayerNumOfSelectedOrgIds(), " org.");
        String columnNameIsNotNull = generateColumnNameIsNotNull(reportVM, isSetadAndOstanInSameCase);
        String generateGroupByStatement = generateGroupByStatement(reportVM, isSetadAndOstanInSameCase);

        String query5 = " ";
        if (reportVM.getShowCortexColumn() != null && reportVM.getShowCortexColumn()) {
            String queryTemp = generateOuterWhereStatement(columnNameIsNotNull, orgLayerColumnNameForFilter, reportVM.getIsEnable());
            queryTemp = !queryTemp.equals("") ? " and " + queryTemp : queryTemp;
            query5 = queryTemp;
            queryTemp = generateOuterWhereStatementCortex(reportVM.getCortexIds());
            queryTemp = !queryTemp.equals("") ? " and " + queryTemp : queryTemp;
            query5 = query5 + " " + queryTemp;
        }


        return String.format(query,
                generateGroupedColumnForSelect(reportVM, isSetadAndOstanInSameCase),
                queryForDate + generateWhereStatement(reportVM, filtersParam),
                generateOuterWhereStatement(columnNameIsNotNull, orgLayerColumnNameForFilter, reportVM.getIsEnable()),
                generateGroupByStatement,query5);
    }

    private String getQueryForDate(ReportVM reportVM, Map<String, Object> filtersParam) {
        filtersParam.put("beginDate", reportVM.getStartDate().toString());
        filtersParam.put("endDate", reportVM.getEndDate().toString());

        String dateQueryForNewMembership = "  \n( mat.mem_is_first_mem=1 and\n" +
                " mat.mem_start_date BETWEEN to_date(:beginDate, 'yyyy-MM-dd') and to_date(:endDate, 'yyyy-MM-dd') )\n";
        String dateQueryForArchived = "  (  mat.mem_is_archived=1 and mat.mem_archive_type_id is not null and mat.mem_end_date BETWEEN to_date(:beginDate, 'yyyy-MM-dd') and to_date(:endDate, 'yyyy-MM-dd') )\n";
        String query = " (" + dateQueryForNewMembership + " or " + dateQueryForArchived + ") and  ";

        reportVM.setStartDate(null);
        reportVM.setEndDate(null);
        return query;
    }

    String completeProvinceQueryByOrgTypeIds(ReportVM reportVM, String query, Map<String, Object> filtersParam) {
        String columnsForSelect = generateGroupedColumnForSelect(reportVM.getOrgTypeCategory(), false);
        String columnsForGroupBy = generateGroupedColumnForSelect(reportVM.getOrgTypeCategory(), true);

        String orgLayerColumnNameForFilter = getColumnNameByOrgTypeLayerNum(reportVM.getLayerNumOfSelectedOrgIds(), " org.");
        String columnNameIsNotNull = columnsForGroupBy.replaceAll(",", " is not null and ") + " is not null ";

        String query3 =generateOtterWhereStatementByOrgTypes(columnNameIsNotNull, orgLayerColumnNameForFilter, reportVM.getOrgTypeIds(), reportVM.getIsEnable());
        query3 = !query3.equals("") ? "  " + query3 : "";
        List<String> filters = new ArrayList<>();
        setFilter(reportVM.getCortexIds(), "org.org_cortex_id" + " in (:cortexIds) ", filters, "cortexIds", filtersParam);
        if (!filters.isEmpty()){
            query3 = !query3.equals("") ? query3 + " and " + filters.get(0) : "";
        }
        return String.format(query,
                columnsForSelect,
                generateWhereStatement(reportVM, filtersParam),
                query3,
                columnsForGroupBy,
                generateConditionByTotal(reportVM.getMinCount(), reportVM.getMaxCount(), reportVM.getMinOrganizingCount(), reportVM.getMaxOrganizingCount(), filtersParam));
    }

    String completeProvinceQueryByMembershipAgeOrg(ReportVM reportVM, String query, Map<String, Object> filtersParam) {
        String columnsForSelect = generateGroupedColumnForSelect(reportVM.getOrgTypeCategory(), false);
        String columnsForGroupBy = generateGroupedColumnForSelect(reportVM.getOrgTypeCategory(), true);

        String orgLayerColumnNameForFilter = getColumnNameByOrgTypeLayerNum(reportVM.getLayerNumOfSelectedOrgIds(), " org.");
        String columnNameIsNotNull = columnsForGroupBy.replaceAll(",", " is not null and ") + " is not null ";

        String query3 = generateOtterWhereStatementByOrgTypes(columnNameIsNotNull, orgLayerColumnNameForFilter, reportVM.getOrgTypeIds(), reportVM.getIsEnable());
        if (!CollectionUtils.isEmpty((reportVM.getCortexIds()))) {
            query3 = !query3.equals("") ? " org.org_cortex_id in (:cortexIds) and " + query3 : " org.org_cortex_id in (:cortexIds)  ";
        }

        query3 = !query3.equals("") ? " and " + query3 : "";
        String query2 = generateWhereStatement(reportVM, filtersParam, "mat.mem_start_date", "mat.mem_status_id",
                "mat.person_sex_id", "mat.mem_org_cortex_id", "mat.mem_org_type_id", " mat.mem_", "mat.org_is_enable","mat.pv_nationality_id", "mat.pv_religion_type_id");
        query2 = !query2.equals("") ? " and " + query2 : "";

        return String.format(query, columnsForSelect, query2 , query3, columnsForGroupBy );
    }

    private String generateGroupByStatement(ReportVM reportVM, boolean isSetadAndOstanInSameCase) {
        switch (EN_OrgTypeLayerNum.getById(reportVM.getLayerNumOfSelectedOrgIds())) {
            case SETAD:
                if (isSetadAndOstanInSameCase)
                    return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.ostan_id, org.nahiyeh_id\n" : " org.ostan_id\n";
                else
                    return "";
            case OSTAN:
                return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.ostan_id, org.nahiyeh_id\n" : " org.ostan_id\n";
            case NAHIYEH:
                if(Boolean.TRUE.equals(reportVM.getDoesIncludeMarkaz()))
                    return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.nahiyeh_id, org.markaz_id\n" : " org.nahiyeh_id\n";
                else
                    return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.nahiyeh_id, org.hozeh_id\n" : " org.nahiyeh_id\n";
            case MARKAZ:
                return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.markaz_id, org.hozeh_id\n" : " org.markaz_id\n";
            case HOZE:
                return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.hozeh_id, org.paygah_id\n" : " org.hozeh_id\n";
            default:
                return " org.paygah_id\n";
        }
    }

    private String generateGroupedColumnForSelect(ReportVM reportVM, boolean isSetadAndOstanInSameCase) {
        switch (EN_OrgTypeLayerNum.getById(reportVM.getLayerNumOfSelectedOrgIds())) {
            case SETAD:
                if (isSetadAndOstanInSameCase)
                    return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " min(org.ostan_title)     parentOrgTitle,\n min(org.nahiyeh_title)      childOrgTitle,\n" : " min(org.ostan_title)       parentOrgTitle,\n";
                else
                    return " \n";
            case OSTAN:
                return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " min(org.ostan_title)     parentOrgTitle,\n min(org.nahiyeh_title)      childOrgTitle,\n" : " min(org.ostan_title)       parentOrgTitle,\n";
            case NAHIYEH:
                if(Boolean.TRUE.equals(reportVM.getDoesIncludeMarkaz()))
                    return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " min(org.nahiyeh_title)   parentOrgTitle,\n min(org.markaz_title)       childOrgTitle,\n" : " min(org.nahiyeh_title)     parentOrgTitle,\n";
                else
                    return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " min(org.nahiyeh_title)   parentOrgTitle,\n min(org.hozeh_title)        childOrgTitle,\n" : " min(org.nahiyeh_title)     parentOrgTitle,\n";
            case MARKAZ:
                return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " min(org.markaz_title)    parentOrgTitle,\n min(org.hozeh_title)        childOrgTitle,\n" : " min(org.markaz_title)      parentOrgTitle,\n";
            case HOZE:
                return Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " min(org.hozeh_title)     parentOrgTitle,\n min(org.paygah_title)       childOrgTitle,\n" : " min(org.hozeh_title)       parentOrgTitle,\n";
            default:
                return " min(org.paygah_title)      parentOrgTitle,\n";
        }
    }

    private String generateGroupedColumnForSelect(EN_LayerNum layerNum, boolean getColumnIds) {
        if(getColumnIds)
            return Arrays.stream(ORG_LEVEL_COLUMN_ID_NAMES, 0, layerNum.ordinal() + 1)
                    .reduce((i,j)-> i.concat(", ").concat(j))
                    .orElse("");
        else
            return Arrays.stream(ORG_LEVEL_COLUMN_TITLE_NAMES, 0, layerNum.ordinal() + 1)
                    .reduce((i,j)-> i.concat(", ").concat(j))
                    .orElse("");
    }

    String generateWhereStatement(ReportVM reportVM, Map<String, Object> filtersParam) {
        return generateWhereStatement(reportVM, filtersParam, "mat.mem_start_date", "mat.mem_status_id",
                "mat.person_sex_id", "mat.mem_org_cortex_id", "mat.mem_org_type_id",
                PRE_NAME_OF_ABSORPTION_ORG_COLUMN, "mat.org_is_enable" ,"mat.pv_nationality_id", "mat.pv_religion_type_id");
    }

    String generateWhereStatement(ReportVM reportVM, Map<String, Object> filtersParam, String startDateColumnName,
                                    String membershipStatusColumnName, String personSexIdColumnName, String cortexIdColumnName,
                                  String orgTypeColumnName, String prefixOrgColumnName, String orgIsEnableColumnName, String pvNationalityIdColumnName, String pvReligionTypeIdColumnName) {
        List<String> filters = new ArrayList<>();

        setFilter(reportVM.getPvNationalityId(), pvNationalityIdColumnName + " in (:pvNationalityIds) ", filters, "pvNationalityIds", filtersParam);
        setFilter(reportVM.getPvReligionTypeId(), pvReligionTypeIdColumnName + " in (:pvReligionTypeIds) ", filters, "pvReligionTypeIds", filtersParam);

        filters.add(filterOfCurrentUserLayerNum(reportVM.getCurrentUserOrgTypeLayerNum(), reportVM.getCurrentUserOrgId(), filtersParam, prefixOrgColumnName));

        setFilter(reportVM.getStartDate(),startDateColumnName + " >= to_date(:beginDate, 'yyyy-MM-dd') ", filters, "beginDate", filtersParam);
        setFilter(reportVM.getEndDate(),startDateColumnName + " <= to_date(:endDate, 'yyyy-MM-dd' )", filters, "endDate", filtersParam);

        setFilter(reportVM.getPvSexTypeIds(), personSexIdColumnName + " in (:psnSexIds) ", filters, "psnSexIds", filtersParam);
        setFilter(reportVM.getPvMembershipStatusIds(), membershipStatusColumnName + " in (:membershipStatusIds) ", filters, "membershipStatusIds", filtersParam);

        if (CollectionUtils.isNotEmpty(reportVM.getOrgIds())) {
            if (!reportVM.getLayerNumOfSelectedOrgIds().equals(reportVM.getCurrentUserOrgTypeLayerNum())) {
                setFilter(reportVM.getOrgIds(),
                        getColumnNameByOrgTypeLayerNum(reportVM.getLayerNumOfSelectedOrgIds(), prefixOrgColumnName) + " in (:orgIds) ", filters, "orgIds", filtersParam);
            } else {
                filtersParam.put("orgIds", reportVM.getOrgIds());
            }
        }


        setFilter(reportVM.getCortexIds(), cortexIdColumnName + " in (:cortexIds) ", filters, "cortexIds", filtersParam);

        setFilter(reportVM.getOrgTypeIds(),orgTypeColumnName + " in (:orgTypeIds) ", filters, "orgTypeIds", filtersParam);

        setFilter(reportVM.getIsEnable(), orgIsEnableColumnName + " =:isEnable  ", filters, "isEnable", filtersParam);

        return filters.stream().filter(i-> !i.equals("")).collect(Collectors.joining("\n and "));
    }

    private <T> void setFilter(T value, String conditionStatement, List<String> filters, String paramKey, Map<String, Object> filtersParam) {
        if (value != null) {
            if (value instanceof ArrayList && ((List) value).size() == 0) {
                return;
            }

            if (value instanceof LocalDate) {
                filtersParam.put(paramKey, value.toString());
                filters.add(conditionStatement);
            } else {
                filtersParam.put(paramKey, value);
                filters.add(conditionStatement);
            }
        }
    }

    private String generateConditionByTotal(Long minMembershipCount, Long maxMembershipCount, Long minOrganizingCount, Long maxOrganizingCount, Map<String, Object> filtersParam) {
        String membershipCountCondition;
        String organizingCountCondition;
        String result = "";
        membershipCountCondition = generateConditionMembershipCount(minMembershipCount, maxMembershipCount, filtersParam);
        organizingCountCondition = generateConditionOrganizingCount(minOrganizingCount, maxOrganizingCount, filtersParam);

        if (!membershipCountCondition.equals("")) {
            result = "where " + membershipCountCondition;

            if (!organizingCountCondition.equals(""))
                result += " and " + organizingCountCondition;
        } else if (!organizingCountCondition.equals("")) {
            result = "where " + organizingCountCondition;
        }

        return result;
    }

    private String generateConditionMembershipCount(Long minMembershipCount, Long maxMembershipCount, Map<String, Object> filtersParam) {
        if (minMembershipCount != null && maxMembershipCount != null) {
            filtersParam.put("minMembershipCount", minMembershipCount);
            filtersParam.put("maxMembershipCount", maxMembershipCount);
            return WHERE_TOTAL_MEMBERSHIP_COLUMNS + " between :minMembershipCount and :maxMembershipCount";
        } else if (minMembershipCount != null) {
            filtersParam.put("minMembershipCount", minMembershipCount);
            return WHERE_TOTAL_MEMBERSHIP_COLUMNS + " >= :minMembershipCount";
        } else if (maxMembershipCount != null) {
            filtersParam.put("maxMembershipCount", maxMembershipCount);
            return WHERE_TOTAL_MEMBERSHIP_COLUMNS + " <= :maxMembershipCount";
        } else
            return "";
    }

    private String generateConditionOrganizingCount(Long minOrganizingCount, Long maxOrganizingCount, Map<String, Object> filtersParam) {
        if (minOrganizingCount != null && maxOrganizingCount != null) {
            filtersParam.put("minOrganizingCount", minOrganizingCount);
            filtersParam.put("maxOrganizingCount", maxOrganizingCount);
            return WHERE_TOTAL_ORGANIZING_COLUMNS + " between :minOrganizingCount and :maxOrganizingCount";
        } else if (minOrganizingCount != null) {
            filtersParam.put("minOrganizingCount", minOrganizingCount);
            return WHERE_TOTAL_ORGANIZING_COLUMNS + " >= :minOrganizingCount";
        } else if (maxOrganizingCount != null) {
            filtersParam.put("maxOrganizingCount", maxOrganizingCount);
            return WHERE_TOTAL_ORGANIZING_COLUMNS + " <= :maxOrganizingCount";
        } else
            return "";
    }
    private String generateOtterWhereStatementByOrgTypes(String columnNameIsNotNull, String orgLayerColumnNameForFilter, List<Long> orgTypeIds, Boolean isEnable) {
        String commonConditions = generateOuterWhereStatement(columnNameIsNotNull, orgLayerColumnNameForFilter, isEnable);

        if (!commonConditions.equals("")) {
            if(CollectionUtils.isNotEmpty(orgTypeIds))
                return commonConditions + " and org.org_type_id in (:orgTypeIds) \n";
            else
                return commonConditions;
        } else if(CollectionUtils.isNotEmpty(orgTypeIds))
            return " org.org_type_id in (:orgTypeIds) \n";
        else
            return  "";
    }

    private String generateOuterWhereStatement(String columnNameIsNotNull, String orgLayerColumnNameForFilter, Boolean isEnable) {
        String queryIsEnable = "";
        if(isEnable!= null) {
            if (!orgLayerColumnNameForFilter.equals("") || !columnNameIsNotNull.equals(""))
                queryIsEnable = " and  org.org_is_enable= " + (isEnable.equals(true) ? "1 " : "0 ");
            else queryIsEnable = " org.org_is_enable= " + (isEnable.equals(true) ? "1 " : "0 ");
        }

        if (!columnNameIsNotNull.equals("")) {
            if(!orgLayerColumnNameForFilter.equals(""))
                return columnNameIsNotNull + " \nand " + orgLayerColumnNameForFilter + " in (:orgIds) \n" + queryIsEnable ;
            else
                return columnNameIsNotNull + " \n" + queryIsEnable ;
        } else if(!orgLayerColumnNameForFilter.equals(""))
            return orgLayerColumnNameForFilter + " in (:orgIds) \n" + queryIsEnable ;
        else
            return  "" + queryIsEnable ;
    }

    String generateOuterWhereStatementCortex(List<Long> cortexIds) {
        if (CollectionUtils.isEmpty(cortexIds))
            return EXCLUDE_SPECIFIC_CORTEX_IDS_CRX;
        else
            return " crx.id in (:cortexIds)";
    }

    private String filterOfCurrentUserLayerNum(Float orgTypeLayerNum, Long currentUserOrgId, Map<String, Object> filtersParam, String prefixOrgColumnName) {
        String result;
        result = getColumnNameByOrgTypeLayerNum(orgTypeLayerNum, prefixOrgColumnName);

        if (!result.equals("")) {
            result += " = :currentUserOrgId";
            filtersParam.put("currentUserOrgId", currentUserOrgId);
        }

        return result;
    }

    private String getColumnNameByOrgTypeLayerNum(Float orgTypeLayerNum, String preColumnName) {
        return switch (EN_OrgTypeLayerNum.getById(orgTypeLayerNum)) {
            case OSTAN -> preColumnName + "ostan_id";
            case NAHIYEH -> preColumnName + "nahiyeh_id";
            case MARKAZ -> preColumnName + "markaz_id";
            case HOZE -> preColumnName + "hozeh_id";
            case PAYGAH -> preColumnName + "paygah_id";
            default -> "";
        };
    }

    private String generateColumnNameIsNotNull(ReportVM reportVM, boolean isSetadAndOstanInSameCase) {
        String result;
        switch (EN_OrgTypeLayerNum.getById(reportVM.getLayerNumOfSelectedOrgIds())) {
            case SETAD:
                if (isSetadAndOstanInSameCase)
                    result = Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.nahiyeh_id " : " org.ostan_id ";
                else
                    result = " org.ostan_id ";
                break;
            case OSTAN:
                result = Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.nahiyeh_id " : "";
                break;
            case NAHIYEH:
                if (Boolean.TRUE.equals(reportVM.getDoesIncludeMarkaz())) {
                    result = Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.markaz_id " : "";
                    break;
                } else {
                    result = Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.hozeh_id " : "";
                    break;
                }
            case MARKAZ:
                result = Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.hozeh_id " : "";
                break;
            case HOZE:
                result = Boolean.TRUE.equals(reportVM.getCalculateOrgChildren()) ? " org.paygah_id " : "";
                break;
            default:
                result = "";
        }

        return !result.equals("") ? result + " is not null " : "";
    }
}
