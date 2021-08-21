package fr.Indyuce.throwables.api.event;

import fr.Indyuce.throwables.throwable.ThrownItem;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.HandlerList;

public class ThrowableLandEvent extends PlayerDataEvent {
    private final ThrownItem item;
    private final Block hitBlock;
    private final BlockFace hitFace;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a throwable lands on the ground.
     *
     * @param item     Thrown item
     * @param hitBlock Block hit by the throwable
     * @param hitFace  Hit block face
     */
    public ThrowableLandEvent(ThrownItem item, Block hitBlock, BlockFace hitFace) {
        super(item.getPlayerData());

        this.item = item;
        this.hitBlock = hitBlock;
        this.hitFace = hitFace;
    }

    public ThrownItem getThrowable() {
        return item;
    }

    public Block getHitBlock() {
        return hitBlock;
    }

    public BlockFace getHitFace() {
        return hitFace;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
