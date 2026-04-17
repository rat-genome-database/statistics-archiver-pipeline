# statistics-archiver-pipeline

Generates comprehensive statistics for all objects in the RGD database and stores them for subsequent viewing in the Score Board Tool. Statistics are generated weekly.

## What it computes

For each species in RGD:

- **Object counts** -- active, withdrawn, and retired RGD objects
- **Gene type** and **strain type** distributions
- **QTL inheritance type** distributions
- **Protein interaction** counts
- **Reference statistics** -- objects with references, annotated references, reference sequences
- **External database (XDB)** cross-reference counts
- **Ontology statistics** -- term counts, annotated terms, annotation counts (total, manual, portal)

Object-specific statistics (genes, QTLs, strains, variants, cell lines):
- Objects with XDB entries
- Ontology annotation and annotated object counts
- Portal and manual annotation counts

## How it works

1. Iterates through all species types defined in RGD.
2. For each species, queries the database for 13+ statistical categories.
3. For each of 6 object types, computes 5 additional statistic categories.
4. Persists all results via `StatisticsDAO.persistStatMap()` for the Score Board Tool to display.
