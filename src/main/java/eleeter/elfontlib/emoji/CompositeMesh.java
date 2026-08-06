package eleeter.elfontlib.emoji;

import eleeter.elfontlib.render.MeshData;

public class CompositeMesh
{
    public final MeshData textMesh;
    public final MeshData emojiMesh;

    public CompositeMesh(MeshData textMesh, MeshData emojiMesh)
    {
        this.textMesh  = textMesh;
        this.emojiMesh = emojiMesh;
    }
}
