package cn.iselab.mooctest.lit.dao;

import cn.iselab.mooctest.lit.model.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface ScriptDao extends JpaRepository<Script, Long> {
    Logger LOGGER = LoggerFactory.getLogger(ScriptDao.class);

    default Script get(Long id) {
        Optional<Script> optionalScript = findById(id);
        if (optionalScript.isPresent()) {
            return optionalScript.get();
        } else {
            LOGGER.error("Cannot find script {} in database.", id);
        }
        return null;
    }

    @Modifying
    @Query(value = "update Script as s set s.currentStep=:currentStep, s.dirsLocation=:dirsLocation " +
            "where s.scriptId=:scriptId")
    void update(@Param("currentStep") int currentStep,
                @Param("dirsLocation") String dirsLocation,
                @Param("scriptId") Long scriptId);

}
