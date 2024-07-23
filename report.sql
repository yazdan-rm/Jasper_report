-- auto-generated definition
create table MEM$MEMBERSHIP
(
    ID                                NUMBER(19) not null
        primary key,
    IP                                VARCHAR2(255 char),
    VERSION                           NUMBER(19),
    CODING                            VARCHAR2(255 char),
    CREATED_BY                        NUMBER(19),
    CREATED_DATE                      TIMESTAMP(6),
    DELETED_BY                        NUMBER(19),
    DELETED_DATE                      TIMESTAMP(6),
    DESCRIPTION                       VARCHAR2(255 char),
    GROUP_TAG                         VARCHAR2(255 char),
    IS_CONFIRMED                      NUMBER(1),
    IS_DELETED                        NUMBER(1)  not null,
    FINAL_FLAG                        NUMBER(1),
    IS_LOCKED                         NUMBER(1),
    IS_SECURE                         NUMBER(1),
    STAR_FLAG                         NUMBER(1),
    IS_TEMP_DELETED                   NUMBER(1),
    LEVEL_FLAG                        NUMBER(10),
    PERMISSION_FLAG                   NUMBER(10),
    FK_PV_STEREO_TYPE_ID              NUMBER(19),
    REQUEST_ID                        NUMBER(19),
    ROW_LEVEL_ID                      NUMBER(19),
    FK_SYS_SCOPE_ID                   NUMBER(19),
    SYSTEM_TAG                        VARCHAR2(255 char),
    UPDATED_BY                        NUMBER(19),
    UPDATED_DATE                      TIMESTAMP(6),
    USER_TAG                          VARCHAR2(255 char),
    ZONE_BRANCH_ID                    NUMBER(19),
    ZONE_CHART_ID                     NUMBER(19),
    ZONE_CODE                         VARCHAR2(255 char),
    ZONE_ORG_ID                       NUMBER(19),
    ZONE_POST_ID                      NUMBER(19),
    END_DATE                          DATE,
    IS_ARCHIVED                       NUMBER(1),
    IS_SHARED_FILE                    NUMBER(1),
    FK_ORG_CORTEX_ID                  NUMBER(19),
    FK_ORG_ID                         NUMBER(19),
    FK_PV_ARCHIVE_TYPE_ID             NUMBER(19),
    FK_PV_MEM_STATUS_ID               NUMBER(19),
    FK_PV_MEMBERSHIP_TYPE_ID          NUMBER(19),
    START_DATE                        DATE,
    FK_MEM_TYPE_LEVEL_ID              NUMBER(19)
        constraint FK7BR0UYSTLR8QR3RRQ7XGPLLAXa
            references BI$MEMBERSHIP_TYPE_LEVEL,
    FK_PERSON_ID                      NUMBER(19)
        constraint FK2DWVATXWF5WJE9EN3LPNLBDNB
            references PSN$PERSON,
    PRIORITY                          NUMBER(10),
    IS_LAST_MEM                       NUMBER(1),
    INITIAL_START_DATE                DATE,
    IS_FIRST_MEM                      NUMBER(1),
    IS_MEM_STATUS_ID_CHANGED          NUMBER(1),
    FK_PV_DOWNGRADE_TYPE_ID           NUMBER(19),
    FK_PV_DOWNGRADE_CATEGORY_ID       NUMBER(19),
    DOWNGRADE_REASON_DESCRIPTION      VARCHAR2(255 char),
    FK_PV_DOWNGRADE_REASON_ID         NUMBER(19),
    FILE_NUMBER                       VARCHAR2(255 char),
    FK_MEM_CORTEX_DOWNGRADE_REASON_ID NUMBER(19)
        constraint FKCXFEXGP8GEECPM9I58W8JSF1L
            references MEM$CORTEX_DOWNGRADE_REASON
)
/

