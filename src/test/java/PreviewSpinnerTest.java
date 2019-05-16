import com.badlogic.gdx.graphics.Pixmap;
import warpwriter.model.IModel;
import warpwriter.model.TurnModel;
import warpwriter.view.VoxelDraw;
import warpwriter.view.render.VoxelPixmapRenderer;

public class PreviewSpinnerTest {

    public Pixmap[] spin(IModel model) {
        return spin(new IModel[]{model});
    }

    public Pixmap[] spin(IModel models[]) {
        final int width = width(models), height = height(models);
        final TurnModel turnModel = new TurnModel();
        Pixmap[] result = new Pixmap[models.length * 8];
        for (int z = 0; z < 4; z++) {
            for (int model = 0; model < models.length; model++) {
                turnModel.set(models[model]);
                result[z * 8 + model] = draw(turnModel, false, width, height);
                result[z * 8 + model + 4] = draw(turnModel, true, width, height);
            }
            turnModel.turner().clockX();
        }
        return result;
    }

    protected VoxelPixmapRenderer renderer;

    public Pixmap draw(TurnModel turnModel) {
        return draw(turnModel, true);
    }

    public Pixmap draw(TurnModel turnModel, final boolean z45) {
        return draw(turnModel, z45, VoxelDraw.isoWidth(turnModel), VoxelDraw.isoHeight(turnModel));
    }

    public Pixmap draw(TurnModel turnModel, final int width, final int height) {
        return draw(turnModel, true, width, height);
    }

    public Pixmap draw(TurnModel turnModel, final boolean z45, final int width, final int height) {
        return draw(turnModel, z45, width, height, 3);
    }

    public Pixmap draw(TurnModel turnModel, final boolean z45, final int width, final int height, final int angle) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        renderer.set(pixmap);

        final int sizeX = (z45 ? VoxelDraw.isoWidth(turnModel) : turnModel.sizeY() - 1),
                offCenter = sizeX / 2;
        final int offsetX = 0, offsetY = 0;

        switch (angle) {
            case 0: // Bottom
                VoxelDraw.drawBottom(turnModel, renderer
                        .setFlipX(false).setFlipY(false)
                        .setOffset(offsetX - offCenter * 6, offsetY)
                );
                break;
            case 1: // Below
                if (z45) {
                    renderer.color().set(renderer.color().direction().flipY());
                    turnModel.turner().clockY().clockY().clockZ();
                    VoxelDraw.drawIso(turnModel, renderer
                            .setFlipX(true).setFlipY(true)
                            .setOffset(
                                    offsetX + (offCenter - 1) * 2,
                                    offsetY + (int) (VoxelDraw.isoHeight(turnModel))
                            )
                    );
                    turnModel.turner().counterZ().counterY().counterY();
                    renderer.color().set(renderer.color().direction().flipY());
                } else {
                    renderer.color().set(renderer.color().direction().opposite());
                    turnModel.turner().clockY().clockY().clockZ().clockZ();
                    VoxelDraw.drawAbove(turnModel, renderer
                            .setFlipX(true).setFlipY(true)
                            .setOffset(
                                    offsetX + (offCenter + 1) * 6,
                                    offsetY + (int) ((turnModel.sizeX() + turnModel.sizeZ()) * 4)
                            )
                    );
                    turnModel.turner().counterZ().counterZ().counterY().counterY();
                    renderer.color().set(renderer.color().direction().opposite());
                }
                break;
            case 2: // Side
                if (z45)
                    VoxelDraw.draw45Peek(turnModel, renderer
                            .setFlipX(false).setFlipY(false)
                            .setOffset(offsetX - offCenter * 2, offsetY)
                    );
                else
                    VoxelDraw.drawRightPeek(turnModel, renderer
                            .setFlipX(false).setFlipY(false)
                            .setOffset(offsetX - offCenter * 6, offsetY)
                    );
                break;
            case 3: // Above
                if (z45)
                    VoxelDraw.drawIso(turnModel, renderer
                            .setFlipX(false).setFlipY(false)
                            .setOffset(offsetX - offCenter * 2, offsetY)
                    );
                else
                    VoxelDraw.drawAbove(turnModel, renderer
                            .setFlipX(false).setFlipY(false)
                            .setOffset(offsetX - offCenter * 6, offsetY)
                    );
                break;
            case 4: // Top
                VoxelDraw.drawTop(turnModel, renderer
                        .setFlipX(false).setFlipY(false)
                        .setOffset(offsetX - offCenter * 6, offsetY)
                );
                break;
            default:
                throw new IllegalStateException("Angle set to unacceptable value: " + angle);
        }
        return pixmap;
    }

    public static int width(IModel models[]) {
        int width = VoxelDraw.isoWidth(models[0]);
        for (IModel model : models) {
            int newWidth = VoxelDraw.isoWidth(model);
            if (newWidth > width) width = newWidth;
        }
        return width;
    }

    public static int height(IModel models[]) {
        int height = VoxelDraw.isoHeight(models[0]);
        for (IModel model : models) {
            int newHeight = VoxelDraw.isoHeight(model);
            if (newHeight > height) height = newHeight;
        }
        return height;
    }
}
