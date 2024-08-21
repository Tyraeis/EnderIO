package com.enderio.modconduits.mods.mekanism;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.ConduitNetwork;
import com.enderio.conduits.api.ConduitNode;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.List;

public class ChemicalTicker extends MultiCapabilityAwareConduitTicker<ChemicalConduit, IChemicalHandler<?, ?>> {

    @SafeVarargs
    public ChemicalTicker(BlockCapability<? extends IChemicalHandler<?, ?>, Direction>... capabilities) {
        super(capabilities);
    }

    @Override
    protected void tickCapabilityGraph(
        ChemicalConduit conduit,
        List<CapabilityConnection<IChemicalHandler<?, ?>>> insertCaps,
        List<CapabilityConnection<IChemicalHandler<?, ?>>> extractCaps, ServerLevel level,
        ConduitNetwork graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {

        for (var extract : extractCaps) {
            tickExtractCapability(conduit, extract.capability(), extract.node(), insertCaps);
        }
    }

    private <C extends Chemical<C>, S extends ChemicalStack<C>> void tickExtractCapability(ChemicalConduit conduit, IChemicalHandler<C, S> extractHandler,
        ConduitNode node, List<CapabilityConnection<IChemicalHandler<?, ?>>> insertCaps) {

        ChemicalConduitData data = node.getOrCreateData(MekanismModule.CHEMICAL_DATA_TYPE.get());

        ChemicalType extractType = ChemicalType.getTypeFor(extractHandler);
        S result;
        if (!data.lockedChemical.isEmpty()) {
            if (data.lockedChemical.getChemicalType() != extractType) {
                return;
            }
            result = extractHandler.extractChemical((S) data.lockedChemical.getChemical().getStack(conduit.transferRate()), Action.SIMULATE);
        } else {
            result = extractHandler.extractChemical(conduit.transferRate(), Action.SIMULATE);
        }
        if (result.isEmpty()) {
            return;
        }

        long transferred = 0;
        for (var insert : insertCaps) {
            ChemicalType insertType = ChemicalType.getTypeFor(insert.capability());
            if (extractType != insertType) {
                continue;
            }
            IChemicalHandler<C, S> destinationHandler = (IChemicalHandler<C, S>) insert.capability();
            S transferredChemical;
            if (!data.lockedChemical.isEmpty()) {
                transferredChemical = tryChemicalTransfer(destinationHandler, extractHandler, (S) data.lockedChemical.getChemical().getStack(conduit.transferRate() - transferred), true);
            } else {
                transferredChemical = tryChemicalTransfer(destinationHandler, extractHandler, conduit.transferRate() - transferred, true);
            }

            transferred += transferredChemical.getAmount();
            if (transferred >= conduit.transferRate()) {
                break;
            }
        }
    }

    private static <C extends Chemical<C>, S extends ChemicalStack<C>> S tryChemicalTransfer(IChemicalHandler<C, S> chemicalDestination, IChemicalHandler<C, S> chemicalSource, long maxAmount, boolean doTransfer) {
        var drainable = chemicalSource.extractChemical(maxAmount, Action.SIMULATE);
        if (!drainable.isEmpty()) {
            return tryChemicalTransfer_Internal(chemicalDestination, chemicalSource, drainable, doTransfer);
        }
        return chemicalSource.getEmptyStack();
    }

    private static <C extends Chemical<C>, S extends ChemicalStack<C>> S tryChemicalTransfer(IChemicalHandler<C, S> chemicalDestination, IChemicalHandler<C, S> chemicalSource, S resource, boolean doTransfer) {
        var drainable = chemicalSource.extractChemical(resource, Action.SIMULATE);
        if (!drainable.isEmpty() && resource.equals(drainable)) {
            return tryChemicalTransfer_Internal(chemicalDestination, chemicalSource, drainable, doTransfer);
        }
        return chemicalSource.getEmptyStack();
    }

    private static <C extends Chemical<C>, S extends ChemicalStack<C>> S tryChemicalTransfer_Internal(IChemicalHandler<C, S> chemicalDestination, IChemicalHandler<C, S> chemicalSource, S drainable, boolean doTransfer) {
        long fillableAmount = drainable.getAmount() - chemicalDestination.insertChemical(drainable, Action.SIMULATE).getAmount();
        if (fillableAmount > 0) {
            drainable.setAmount(fillableAmount);
            if (doTransfer) {
                var drained = chemicalSource.extractChemical(drainable, Action.EXECUTE);
                if (!drained.isEmpty()) {
                    drained.setAmount(chemicalDestination.insertChemical(drained, Action.EXECUTE).getAmount());
                    return drained;
                }
            } else {
                return drainable;
            }
        }
        return chemicalSource.getEmptyStack();
    }
}