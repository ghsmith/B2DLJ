package b2dlj.finder;

import b2dlj.data.Block;
import b2dlj.data.Part;
import b2dlj.data.Slide;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartFinder {

    Connection connClr;
    PreparedStatement pstmt;

    public PartFinder(Connection connClr) throws SQLException {

        this.connClr = connClr;
        pstmt = connClr.prepareStatement("""
select
  substring(sdm.spec_number_ln1, len(lci.case_num) + 2, 10) part_desig,
  cast(replace(substring(odm.container_name, len(sdm.spec_number_ln1) + 1, 2), '-', '') as int) block_no,
  cast(substring(substring(odm.container_name, len(sdm.spec_number_ln1) + 1, 10), charindex('-', substring(odm.container_name, len(sdm.spec_number_ln1) + 1, 10)) + 1, 10) as int) slide_no,
  (select name from zc_specimen_type where specimen_type_c = sdm.specimen_type_c) spec_type,
  (select name from zc_specimen_source where specimen_source_c = sdm.spec_source_c) source,
  (select protocol_name from ap_specimen_desc asd join protocol_db_main pdm on(asd.ap_spec_descr_id = pdm.protocol_id) where asd.specimen_id = sdm.specimen_id) protocol,
  (select ap_spec_descr_cmt from ap_specimen_desc where specimen_id = sdm.specimen_id) descr,
  zt.name stain
from
  lab_case_info lci
  join spec_db_main sdm on(lci.requisition_id = sdm.case_id)
  join spec_task_list stl on(sdm.specimen_id = stl.specimen_id)
  join spec_task_list_sub stls on(sdm.specimen_id = stls.specimen_id and stl.line = stls.group_line)
  join ovc_db_main odm on(stls.task_linked_sctr_id = odm.container_id)
  join zc_task zt on(stl.task_c = zt.task_c)
where
  stl.task_deleted_yn = 'N'
  and stl.task_inst is not null
  and stl.task_action_c = 3
  and lci.requisition_id = ?
order by
  1,2,3
        """);

    }

    public List<Part> getByCaseId(String caseId) throws SQLException{
        List<Part> parts = new ArrayList<>();
        pstmt.clearParameters();
        pstmt.setString(1, caseId);
        ResultSet rs = pstmt.executeQuery();
        Part currentPart = null;
        Block currentBlock = null;
        while(rs.next()){
            if(currentPart == null || !currentPart.partDesig.equals(rs.getString("part_desig"))) {
                Part part = new Part();
                parts.add(part);
                part.partDesig = rs.getString("part_desig");
                part.partType = rs.getString("spec_type");
                part.source = rs.getString("source");
                part.protocol = rs.getString("protocol");
                part.descr = rs.getString("descr");
                part.blocks = new ArrayList<>();
                currentPart = part;
                currentBlock = null;
            }
            if(currentBlock == null || currentBlock.blockNo != rs.getInt("block_no")) {
                Block block = new Block();
                currentPart.blocks.add(block);
                block.blockNo = rs.getInt("block_no");
                block.slides = new ArrayList<>();
                currentBlock = block;
            }
            Slide slide = new Slide();
            currentBlock.slides.add(slide);
            slide.slideNo = rs.getInt("slide_no");
            slide.stain = rs.getString("stain");
        }
        return parts;
    }

}