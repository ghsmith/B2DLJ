package b2dlj.finder;

import b2dlj.data.PathologyCase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PathologyCaseFinder{

    Connection connClr;
    PreparedStatement pstmt;

    public PathologyCaseFinder(Connection connClr) throws SQLException {

        this.connClr = connClr;
        pstmt = connClr.prepareStatement("""
select
  lcdm.case_id,
  lci.case_num,
  lcdm.case_coll_dttm,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (
        select
          max(rog.order_id) max_order_id
        from
          lab_case_info lci
          join req_order_group rog on(lci.requisition_id = rog.requisition_id)
        where
          lci.requisition_id = lcdm.case_id
      ) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '180'
  ) final_dx,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (
        select
          max(rog.order_id) max_order_id
        from
          lab_case_info lci
          join req_order_group rog on(lci.requisition_id = rog.requisition_id)
        where
          lci.requisition_id = lcdm.case_id
      ) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '181'
  ) addendum_dx,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (
        select
          max(rog.order_id) max_order_id
        from
          lab_case_info lci
          join req_order_group rog on(lci.requisition_id = rog.requisition_id)
        where
          lci.requisition_id = lcdm.case_id
      ) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '244'
  ) dx_comment,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (
        select
          max(rog.order_id) max_order_id
        from
          lab_case_info lci
          join req_order_group rog on(lci.requisition_id = rog.requisition_id)
        where
          lci.requisition_id = lcdm.case_id
      ) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '179'
  ) special_stains,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (
        select
          max(rog.order_id) max_order_id
        from
          lab_case_info lci
          join req_order_group rog on(lci.requisition_id = rog.requisition_id)
        where
          lci.requisition_id = lcdm.case_id
      ) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '1230000057'
  ) gross_descr,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (
        select
          max(rog.order_id) max_order_id
        from
          lab_case_info lci
          join req_order_group rog on(lci.requisition_id = rog.requisition_id)
        where
          lci.requisition_id = lcdm.case_id
      ) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '140'
  ) micro_descr,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (
        select
          max(rog.order_id) max_order_id
        from
          lab_case_info lci
          join req_order_group rog on(lci.requisition_id = rog.requisition_id)
        where
          lci.requisition_id = lcdm.case_id
      ) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '135'
  ) clinical_hx
from
  patient p
  join lab_case_db_main lcdm on(p.pat_id = lcdm.case_pat_id)
  join lab_case_info lci on(lcdm.case_id = lci.requisition_id)
where
  lci.case_num = ?
order by
  lcdm.case_coll_dttm
        """);

    }

    public List<PathologyCase> getByAccessionNumber(String accessionNumber) throws SQLException {
        List<PathologyCase> pathologyCases = new ArrayList<>();
        pstmt.clearParameters();
        pstmt.setString(1, accessionNumber);
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()) {
            PathologyCase pathologyCase = new PathologyCase();
            pathologyCases.add(pathologyCase);
            pathologyCase.caseId = rs.getString("case_id");
            pathologyCase.accessionNumber = rs.getString("case_num");
            pathologyCase.collectionDate = new Date(rs.getDate("case_coll_dttm").getTime());
            pathologyCase.finalDx = rs.getString("final_dx");
            pathologyCase.addendumDx = rs.getString("addendum_dx");
            pathologyCase.dxComment = rs.getString("dx_comment");
            pathologyCase.specialStains = rs.getString("special_stains");
            pathologyCase.grossDescr = rs.getString("gross_descr");
            pathologyCase.microDescr = rs.getString("micro_descr");
            pathologyCase.clinicalHx = rs.getString("clinical_hx");
        }
        return pathologyCases;
    }

}