package me.orangemonkey68.injectablerecipes;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InjectableRecipes {
    private static final Map<Class<? extends RecipeHolder>, Map<RecipeType<?>, Map<Identifier, Recipe<?>>>> holders = new Object2ObjectOpenHashMap<>();

//    private InjectableRecipes(){
//
//    }

    public static void register(RecipeHolder holder){
        if(holders.containsKey(holder.getClass())){
            throw new IllegalStateException(String.format("RecipeHolder %s already registered", holder.getClass()));
        }

        LogManager.getLogger().info("registering class {}", holder.getClass().toString());

        holders.put(holder.getClass(), holder.getRecipes());
    }

    public static Map<Class<? extends RecipeHolder>, Map<RecipeType<?>, Map<Identifier, Recipe<?>>>> getHolders(){
        return holders;
    }

    public static Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipesFromHolder(Class <? extends RecipeHolder> holderClass){
        return holders.get(holderClass);
    }

    //noinspection CodeBlock2Expr
    public static Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getAllRecipes(){
        List<Map<RecipeType<?>, Map<Identifier, Recipe<?>>>> holderRecipeMaps = new ArrayList<>(holders.values());
        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipeMap = new Object2ObjectOpenHashMap<>();
        //intellij yells at me if this isn't atomic
        AtomicInteger count = new AtomicInteger();

        holderRecipeMaps.forEach(map -> {
            map.forEach((type, identifierMap) -> {
                identifierMap.forEach((id, recipe) -> {
                    recipeMap.computeIfAbsent(type, k -> new HashMap<>());
                    recipeMap.get(type).put(id, recipe);


                    count.getAndIncrement();
                });
            });
        });

        //delete before prod
        System.out.println(recipeMap);
        System.out.println(holderRecipeMaps);
        System.out.println(holders);

        //keep (delete comment)
        LogManager.getLogger("injectable-recipes").info("Discovered {} recipes to add", count.get());

        return recipeMap;
    }
}
