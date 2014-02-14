package com.jme3.cubed.render;

import com.jme3.cubed.ChunkTerrain;
import com.jme3.cubed.Face;
import com.jme3.cubed.MaterialManager;
import com.jme3.cubed.math.Vector3i;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import java.util.ArrayList;

/**
 *
 * @author Nicholas Minkler <sleaker@gmail.com>
 */


public class GreedyMesher extends VoxelMesher {

    @Override
    public Mesh generateMesh(ChunkTerrain terrain) {
                ArrayList<Vector3f> verts = new ArrayList<>();
        ArrayList<Vector2f> textCoords = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        ArrayList<Float> normals = new ArrayList<>();

        Vector3i tmpI = new Vector3i();
        
        int i, j, k, l, h, w, u, v, n, r, s, t;
 
        Face face = null;
        final int[] x = new int[] {0, 0, 0};
        final int[] q = new int[] {0, 0, 0};
        int[] du = new int[] {0, 0, 0};
        int[] dv = new int[] {0, 0, 0};  
        
        final byte[] mask = new byte[ChunkTerrain.C_SIZE * ChunkTerrain.C_SIZE];
        // First pass is for front face, second pass is for back face
        for (boolean backFace = true, b = false; b != backFace; backFace = backFace && b, b = !b) {
            // Loop over all 3 dimensions
            for (int d = 0; d < 3; d++) {
                u = (d + 1) % 3;
                v = (d + 2) % 3;
                
                x[0] = 0; x[1] = 0; x[2] = 0;
                q[0] = 0; q[1] = 0; q[2] = 0; q[d] = 1;
                
                // Keep track of what face we are computing
                if (d ==0) {
                    face = backFace ? Face.RIGHT : Face.LEFT;
                } else if (d == 1) {
                    face = backFace ? Face.TOP : Face.BOTTOM;
                } else if (d == 2) {
                    face = backFace ? Face.FRONT : Face.BACK;
                }
                // Loop over the entire voxel volume to generate the mask
                for (x[d] = 0; x[d] < ChunkTerrain.C_SIZE; x[d]++) {
                    n = 0;
                    for (x[v] = 0; x[v] < ChunkTerrain.C_SIZE; x[v]++) {
                        for (x[u] = 0; x[u] < ChunkTerrain.C_SIZE; x[u]++) {
                            tmpI.set(x[0], x[1], x[2]);
                            mask[n++] = terrain.isFaceVisible(tmpI, face) ? terrain.getBlock(tmpI) : 0;
                        }
                    }
                    
                    n = 0;
                    for (j = 0; j < ChunkTerrain.C_SIZE; j++) {
                        for (i = 0; i < ChunkTerrain.C_SIZE; ) {
                            // Loop until we find a start point
                            if (mask[n] != 0) {
                                // Find the width of this mask section, w == current width
                                for (w = 1; w + i < ChunkTerrain.C_SIZE && mask[n + w] != 0 && mask[n + w] == mask[n]; w++) { }
                                
                                boolean done = false;
                                // find the height of the mask section, h == current height
                                for (h = 1; j + h < ChunkTerrain.C_SIZE; h++) {
                                    // make sure a whoel row of each height matches the width
                                    for (k = 0; k < w; k++) {
                                        if (mask[n + k + h * ChunkTerrain.C_SIZE] == 0 || mask[n + k + h * ChunkTerrain.C_SIZE] != mask[n]) {
                                            done = true;
                                            break;
                                        }
                                    }
                                    if (done) break;
                                }
                                x[u] = i;
                                x[v] = j;
                                du[0] = 0; du[1] = 0; du[2] = 0; du[u] = w;
                                dv[0] = 0; dv[1] = 0; dv[2] = 0; dv[v] = h;
                                if (!backFace) {
                                    r = x[0];
                                    s = x[1];
                                    t = x[2];
                                } else {
                                    r = x[0] + q[0];
                                    s = x[1] + q[1];
                                    t = x[2] + q[2];
                                }
                                Vector3f vec0 = new Vector3f(r, s, t);
                                Vector3f vec1 = new Vector3f(r + du[0], s + du[1], t + du[2]);
                                Vector3f vec2 = new Vector3f(r + dv[0], s + dv[1], t + dv[2]);
                                Vector3f vec3 = new Vector3f(r + du[0] + dv[0], s + du[1] + dv[1], t + du[2] + dv[2]);
                                
                                // Each face has a specific order of vertices otherwise the textures rotate incorrectly
                                switch (face) {
                                    case TOP:
                                        writeQuad(verts, textCoords, indices, normals,
                                            vec1, vec3, vec0, vec2, w, h, face, MaterialManager.getInstance().getType(mask[n]).getSkin());
                                        break;
                                    case BOTTOM:
                                        writeQuad(verts, textCoords, indices, normals,
                                            vec3, vec1, vec2, vec0, w, h, face, MaterialManager.getInstance().getType(mask[n]).getSkin());
                                        break;
                                    case LEFT:
                                        writeQuad(verts, textCoords, indices, normals,
                                            vec0, vec2, vec1, vec3, w, h, face, MaterialManager.getInstance().getType(mask[n]).getSkin());
                                        break;
                                    case RIGHT:
                                        //TODO: figure out why right face overwrites transparency
                                        writeQuad(verts, textCoords, indices, normals,
                                            vec2, vec0, vec3, vec1, w, h, face, MaterialManager.getInstance().getType(mask[n]).getSkin());
                                        break;
                                    case FRONT:
                                        writeQuad(verts, textCoords, indices, normals,
                                            vec0, vec1, vec2, vec3, w, h, face, MaterialManager.getInstance().getType(mask[n]).getSkin());
                                        break;
                                    case BACK:
                                        writeQuad(verts, textCoords, indices, normals,
                                            vec1, vec0, vec3, vec2, w, h, face, MaterialManager.getInstance().getType(mask[n]).getSkin());
                                        break;
                                }
                                // Clear the mask
                                for (l = 0; l < h; ++l) {
                                    for (k = 0; k < w; ++k) {
                                        mask[n + k + l * ChunkTerrain.C_SIZE] = 0;
                                    }
                                }
                                // increment i and n
                                i += w;
                                n += w;
                            } else {
                                i++;
                                n++;
                            }
                        }
                    }
                }
            }
        }
        if (indices.isEmpty()) {
            return null;
        }
        return genMesh(verts, textCoords, indices, normals);
    }
    
}
