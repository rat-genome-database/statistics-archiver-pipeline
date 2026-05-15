package edu.mcw.rgd.stats;

import edu.mcw.rgd.dao.impl.StatisticsDAO;
import edu.mcw.rgd.datamodel.RgdId;
import edu.mcw.rgd.datamodel.SpeciesType;
import edu.mcw.rgd.process.MemoryMonitor;
import edu.mcw.rgd.process.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.util.Collection;
import java.util.Map;

/**
 * @author mtutaj
 * @since 6/23/11
 * computes various stats about rgd objects and archives them in the special table in database
 */
public class ScoreBoardArchiver {

    /** sentinel meaning "across all object types" (not a real RGD object key) */
    private static final int OBJECT_KEY_ALL = 0;

    private static final Logger log = LogManager.getLogger("status");

    private String version;
    StatisticsDAO dao = new StatisticsDAO();

    public static void main(String[] args) throws Exception {

        long time0 = System.currentTimeMillis();

        MemoryMonitor memoryMonitor = new MemoryMonitor();
        memoryMonitor.start();

        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        ScoreBoardArchiver sb = (ScoreBoardArchiver) bf.getBean("archiver");

        boolean ok = false;
        try {
            sb.archive();
            ok = true;
        } catch(Exception e) {
            Utils.printStackTrace(e, log);
            throw e;
        } finally {
            memoryMonitor.stop();
            log.info(memoryMonitor.getSummary());
            log.info((ok ? "=== OK === " : "=== FAILED === ") + "elapsed "+ Utils.formatElapsedTime(time0, System.currentTimeMillis())+"\n");
        }
    }

    public void archive() throws Exception{

        long time0 = System.currentTimeMillis();

        log.info(getVersion());
        log.info("   "+dao.getConnectionInfo());

        Collection<Integer> specs = SpeciesType.getSpeciesTypeKeys();

        for (int speciesType : specs) {
            log.info("stats for ["+ SpeciesType.getCommonName(speciesType)+"]");

            persistStats("RGD Object", speciesType, dao.getRGDObjectCount(speciesType));
            persistStats("Active Object", speciesType, dao.getActiveCount(speciesType));
            persistStats("Withdrawn Object", speciesType, dao.getWithdrawnCount(speciesType));
            persistStats("Retired Object", speciesType, dao.getRetiredCount(speciesType));
            persistStats("Protein Interaction", speciesType, dao.getProteinInteractionCount(speciesType));
            persistStats("Gene Type", speciesType, dao.getGeneTypeCount(speciesType));
            persistStats("Strain Type", speciesType, dao.getStrainTypeCount(speciesType));
            persistStats("QTL Inheritance Type", speciesType, dao.getQTLInheritanceTypeCount(speciesType));
            persistStats("Objects With Reference", speciesType, dao.getObjectReferenceCount(speciesType));
            persistStats("Objects With Reference Sequence", speciesType, dao.getObjectWithReferenceSequenceCount(speciesType));
            persistStats("XDB Count", speciesType, dao.getXDBsCount(speciesType));
            persistStats("References with Annotations", speciesType, dao.getAnnotatedReferencesCount(speciesType));

            if( speciesType == SpeciesType.ALL ) {
                persistStats("Ontology Terms", speciesType, dao.getOntologyTermCount());
            }
            persistStats("Ontology Annotated Terms", speciesType, dao.getOntologyAnnotatedTermCount(speciesType));

            int[] objectKeys = {
                    OBJECT_KEY_ALL,
                    RgdId.OBJECT_KEY_GENES,
                    RgdId.OBJECT_KEY_QTLS,
                    RgdId.OBJECT_KEY_STRAINS,
                    RgdId.OBJECT_KEY_VARIANTS,
                    RgdId.OBJECT_KEY_CELL_LINES
            };
            for( int objectKey: objectKeys ) {
                String objectName = RgdId.getObjectTypeName(objectKey);

                persistStats((objectKey==OBJECT_KEY_ALL?"Objects":objectName+"s")+" With XDB",
                        speciesType, dao.getObjectsWithXDBsCount(speciesType, objectKey));

                persistStats("Ontology "+(objectKey==OBJECT_KEY_ALL?"":objectName+" ")+"Annotations",
                        speciesType, dao.getOntologyAnnotationCount(speciesType, objectKey));
                persistStats("Ontology "+(objectKey==OBJECT_KEY_ALL?"Object":objectName)+"s Annotated",
                        speciesType, dao.getOntologyAnnotatedObjectCount(speciesType, objectKey));

                String name = StatisticsDAO.getPortalStatName(speciesType, objectKey);
                persistStats(name, speciesType, dao.getPortalAnnotatedObjectCount(speciesType, objectKey));

                persistStats("Ontology "+(objectKey==OBJECT_KEY_ALL?"":objectName)+" Manual Annotations",
                        speciesType, dao.getOntologyManualAnnotationCount(speciesType, objectKey));
                persistStats("Ontology "+(objectKey==OBJECT_KEY_ALL?"Object":objectName)+"s Manually Annotated",
                        speciesType, dao.getOntologyManuallyAnnotatedObjectCount(speciesType, objectKey));
            }
        }

        log.info("COMPLETE   elapsed "+ Utils.formatElapsedTime(time0, System.currentTimeMillis()));
    }

    private void persistStats(String objectType, int speciesType, Map<String,String> map) throws Exception {

        // don't print if no results
        if( map.size()!=0 ) {
            log.debug("   [" + objectType + "] " + map.size());
        }

        dao.persistStatMap(objectType, speciesType, map);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
