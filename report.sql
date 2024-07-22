select min(mat.field_title)                                                      field_title,
       min(mat.rps_title)                                                        rps_title,
       min(rpst.title)                                                           rps_type_title,
       sum(case when mat.mem_type_id = 143 then 1 else 0 end)                    common_count,
       sum(case when mat.mem_type_id = 144 then 1 else 0 end)                    active_count,
       count(distinct case when mat.fk_pv_status_id = 1 then mat.def_rps_id end) rps_group_count,
       count(case when mat.fk_pv_status_id = 2 then mat.def_rps_id end)          rps_individual_count
from org.org_parent_titles_matv org
         left join (select mat.*
                    FROM REPORT_ORGANIZING_COMPLETE_DEF_RPS_MATV mat
                    where mat.hozeh_id = 50375
                      and mat.field_id in (110)
                      and mat.rps_id in (1242, 1244, 1243, 101, 1105)
                      and mat.def_rps_type_id in (304, 305, 306, 537)
                      and mat.def_rps_cortex_id in (154004)
                      and ((mat.isApproved = 1 and mat.is_need_approve = 1) or mat.is_need_approve = 0)) mat
                   on mat.def_rps_org_id = org.org_id
         inner join ORG.rps$rps_type rpst on mat.def_rps_type_id = rpst.id
where org.is_deleted = 0
  and mat.field_id is not null
  and mat.rps_id is not null
  and org.hozeh_id in (50375)

group by mat.field_id, mat.rps_id, mat.DEF_RPS_TYPE_ID
order by mat.field_id, mat.rps_id;

