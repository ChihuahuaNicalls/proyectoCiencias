package ciencias.Graphs;

import java.io.Serializable;
import java.util.*;

/**
 * Clase compartida para serialización de grafos entre las tres pestañas
 * de la aplicación (Operaciones, Árboles, Representación).
 * 
 * Esta clase permite que los grafos creados en una pestaña puedan ser
 * cargados en otra pestaña sin problemas de compatibilidad.
 */
public class SharedGraphData implements Serializable {
    private static final long serialVersionUID = 1L;

    public Set<String> vertices;
    public List<SharedEdge> edges;
    public boolean isDirected;
    public boolean isWeighted;

    public SharedGraphData() {
        this.vertices = new LinkedHashSet<>();
        this.edges = new ArrayList<>();
        this.isDirected = false;
        this.isWeighted = true;
    }

    public SharedGraphData(Set<String> vertices, List<SharedEdge> edges, boolean isDirected, boolean isWeighted) {
        this.vertices = new LinkedHashSet<>(vertices);
        this.edges = new ArrayList<>(edges);
        this.isDirected = isDirected;
        this.isWeighted = isWeighted;
    }

    @Override
    public String toString() {
        return "SharedGraphData{" +
                "vertices=" + vertices +
                ", edges=" + edges +
                ", isDirected=" + isDirected +
                ", isWeighted=" + isWeighted +
                '}';
    }

    /**
     * Clase interna para representar una arista de forma serializable
     * Compatible con las estructuras Edge de todos los controladores
     */
    public static class SharedEdge implements Serializable {
        private static final long serialVersionUID = 1L;

        public String source;
        public String destination;
        public String label;
        public boolean isSumEdge;
        public boolean isLoop;

        public SharedEdge() {
            this("", "", "");
        }

        public SharedEdge(String source, String destination, String label) {
            this.source = source;
            this.destination = destination;
            this.label = label;
            this.isSumEdge = false;
            this.isLoop = source.equals(destination);
        }

        public SharedEdge(String source, String destination, String label, boolean isSumEdge) {
            this.source = source;
            this.destination = destination;
            this.label = label;
            this.isSumEdge = isSumEdge;
            this.isLoop = source.equals(destination);
        }

        @Override
        public String toString() {
            return "SharedEdge{" +
                    "source='" + source + '\'' +
                    ", destination='" + destination + '\'' +
                    ", label='" + label + '\'' +
                    ", isSumEdge=" + isSumEdge +
                    ", isLoop=" + isLoop +
                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            SharedEdge edge = (SharedEdge) obj;
            return source.equals(edge.source) &&
                    destination.equals(edge.destination) &&
                    label.equals(edge.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, destination, label);
        }
    }
}
