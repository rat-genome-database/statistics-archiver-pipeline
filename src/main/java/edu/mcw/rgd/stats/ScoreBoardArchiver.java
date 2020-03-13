package edu.mcw.rgd.stats;

import edu.mcw.rgd.dao.impl.StatisticsDAO;
import edu.mcw.rgd.datamodel.RgdId;
import edu.mcw.rgd.datamodel.SpeciesType;
import edu.mcw.rgd.process.Utils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.util.*;

/**
 * @author mtutaj
 * @since 6/23/11
 * computes various stats about rgd objects and archives them in the special table in database
 */
public class ScoreBoardArchiver {
    private String version;

    public static void main(String[] args) throws Exception {

        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        ScoreBoardArchiver sb = (ScoreBoardArchiver) bf.getBean("archiver");
        sb.archive();
    }

    StatisticsDAO dao = new StatisticsDAO();

    public void archive() throws Exception{

        long time0 = System.currentTimeMillis();

        System.out.println(getVersion());
        System.out.println("   "+dao.getConnectionInfo());

        Collection<Integer> specs = SpeciesType.getSpeciesTypeKeys();

        for (int speciesType : specs) {
            System.out.println("stats for ["+ SpeciesType.getCommonName(speciesType)+"]");

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

            // added in JUNE 2011 -- MT
            if( speciesType==0 ) {
                persistStats("Ontology Terms", speciesType, dao.getOntologyTermCount());
            }
            persistStats("Ontology Annotated Terms", speciesType, dao.getOntologyAnnotatedTermCount(speciesType));

            // added in MAY 2012 -- MT
            int[] objectKeys = {0, RgdId.OBJECT_KEY_GENES, RgdId.OBJECT_KEY_QTLS, RgdId.OBJECT_KEY_STRAINS};
            for( int objectKey: objectKeys ) {
                String objectName = RgdId.getObjectTypeName(objectKey);

                persistStats((objectKey==0?"Objects":objectName+"s")+" With XDB",
                        speciesType, dao.getObjectsWithXDBsCount(speciesType, objectKey));

                persistStats("Ontology "+(objectKey==0?"":objectName+" ")+"Annotations",
                        speciesType, dao.getOntologyAnnotationCount(speciesType, objectKey));
                persistStats("Ontology "+(objectKey==0?"Object":objectName)+"s Annotated",
                        speciesType, dao.getOntologyAnnotatedObjectCount(speciesType, objectKey));

                String name = StatisticsDAO.getPortalStatName(speciesType, objectKey);
                persistStats(name, speciesType, dao.getPortalAnnotatedObjectCount(speciesType, objectKey));

                persistStats("Ontology "+(objectKey==0?"":objectName)+" Manual Annotations",
                        speciesType, dao.getOntologyManualAnnotationCount(speciesType, objectKey));
                persistStats("Ontology "+(objectKey==0?"Object":objectName)+"s Manually Annotated",
                        speciesType, dao.getOntologyManuallyAnnotatedObjectCount(speciesType, objectKey));
            }
        }

        System.out.print("COMPLETE   elapsed "+ Utils.formatElapsedTime(time0, System.currentTimeMillis()));
    }

    private void persistStats(String objectType, int speciesType, Map<String,String> map) throws Exception {

        System.out.println("   ["+objectType+"] "+map.size());

        dao.persistStatMap(objectType, speciesType, map);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
