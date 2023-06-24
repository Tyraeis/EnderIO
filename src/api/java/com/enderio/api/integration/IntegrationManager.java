package com.enderio.api.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class IntegrationManager {

    private static final List<Integration> ALL_INTEGRATIONS = new ArrayList<>();

    public static <T extends Integration> IntegrationWrapper<T> wrapper(String modid, Supplier<T> integration) {
        return new IntegrationWrapper<>(modid, integration);
    }

    public static void addIntegration(Integration integration) {
        ALL_INTEGRATIONS.add(integration);
    }

    public static boolean noneMatch(Predicate<Integration> predicate) {
        return ALL_INTEGRATIONS.stream().noneMatch(predicate);
    }

    public static boolean allMatch(Predicate<Integration> predicate) {
        return ALL_INTEGRATIONS.stream().allMatch(predicate);
    }

    public static boolean anyMatch(Predicate<Integration> predicate) {
        return ALL_INTEGRATIONS.stream().anyMatch(predicate);
    }

    public static void forAll(Consumer<Integration> consumer) {
        ALL_INTEGRATIONS.forEach(consumer);
    }

    public static <T> Optional<T> getFirst(Function<Integration, Optional<T>> mapper) {
        return ALL_INTEGRATIONS.stream().map(mapper).flatMap(Optional::stream).findFirst();
    }

    public static void executeIf(Predicate<Integration> condition, Consumer<Integration> consumer) {
        for (Integration integration : ALL_INTEGRATIONS) {
            if (condition.test(integration))
                consumer.accept(integration);
        }
    }
    public static <T> List<T> getIf(Predicate<Integration> condition, Function<Integration, T> mapper) {
        List<T> list = new ArrayList<>();
        for (Integration integration : ALL_INTEGRATIONS) {
            if (condition.test(integration))
                list.add(mapper.apply(integration));
        }
        return list;
    }
}