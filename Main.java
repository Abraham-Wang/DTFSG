package DTFSG;

import com.seedfinding.mcbiome.source.BiomeSource;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.rand.seed.WorldSeed;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.misc.SpawnPoint;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.generator.structure.DesertPyramidGenerator;

public class Main {
    public static void main(String[] args) {
        /*
        Goal: playable desert temple seeds
        Requirements:
        - 5 chunks from spawn
        - 7 guaranteed iron, 20 guaranteed flesh
         */
        MCVersion version = MCVersion.v1_21;
        ChunkRand rand = new ChunkRand();
        DesertPyramid temple = new DesertPyramid(version);
        DesertPyramidGenerator dtg = new DesertPyramidGenerator(version);

        for(long structureSeed = 1; structureSeed < 100_000_000L; structureSeed++){
            CPos dtpos = temple.getInRegion(structureSeed,0,0,rand);
            if(dtpos.distanceTo(CPos.ZERO, DistanceMetric.CHEBYSHEV) > 12){
                continue;
            }

            dtg.generate(null,dtpos);
            var loot = temple.getLoot(structureSeed,dtg, rand,false);

            int totalIron = 0;
            int totalFlesh = 0;
            for (var chest : loot){
                totalIron += chest.getCount(Items.IRON_INGOT);
                totalFlesh += chest.getCount(Items.ROTTEN_FLESH);
            }

            if(totalIron < 7 || totalFlesh < 20){
                continue;
            }

            WorldSeed.getSisterSeeds(structureSeed).asStream().boxed().limit(1000).forEach(worldseed -> {
                BiomeSource obs = BiomeSource.of(Dimension.OVERWORLD, version, worldseed);
                if (!temple.canSpawn(dtpos, obs)) {
                    return;
                }

                CPos spawnPos = SpawnPoint.getApproximateSpawn((OverworldBiomeSource) obs).toChunkPos();
                if(spawnPos.distanceTo(dtpos, DistanceMetric.CHEBYSHEV) > 5){
                    return;
                }

                System.out.println(worldseed);
            });
        }
    }
}