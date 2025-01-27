package com.goat.goatae2;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.ILateMixinLoader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.Name("NAE2-Core")
@Optional.Interface(iface = "zone.rong.mixinbooter.ILateMixinLoader", modid = "mixinbooter")
public class GOATAE2MixinPlugin implements IFMLLoadingPlugin,ILateMixinLoader {


	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Nullable
	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	@Optional.Method(modid = "mixinbooter")
	public List<String> getMixinConfigs() {
		//NAE2.setupConfig();

		List mixins = new ArrayList<String>();
//
//		// Core.
		mixins.add("mixins.goatae2.json");

//		for (var feature : Features.values()) {
//			if (!feature.isEnabled()) continue;
//
//			var featureMixins = feature.getMixins();
//			if (featureMixins != null) {
//				for (var featureMixin : featureMixins) {
//					mixins.add(String.format(MIXIN_PATH_FORMAT, featureMixin));
//				}
//			}
//
//			var subFeatures = feature.getSubFeatures();
//			if (subFeatures != null) {
//				for (var subFeature : subFeatures) {
//					if (!subFeature.isEnabled()) continue;
//
//					var subFeatureMixins = subFeature.getMixins();
//					if (subFeatureMixins != null) {
//						mixins.add(String.format(MIXIN_PATH_FORMAT, subFeatureMixins));
//					}
//				}
//			}
//		}

		return mixins;// ImmutableList.copyOf(mixins);
	}
}