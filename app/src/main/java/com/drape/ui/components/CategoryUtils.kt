package com.drape.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.drape.R
import com.drape.data.model.ItemCategory

/**
 * Returns the localized display name for a clothing category using Compose's stringResource.
 * This ensures the UI remains reactive to configuration changes (like language).
 *
 * @param category The [ItemCategory] enum value.
 * @return The localized display string.
 */
@Composable
fun getDisplayNameForCategory(category: ItemCategory): String {
    return when (category) {
        ItemCategory.TOP -> stringResource(R.string.outfit_creator_category_top)
        ItemCategory.BOTTOM -> stringResource(R.string.outfit_creator_category_bottom)
        ItemCategory.SHOES -> stringResource(R.string.outfit_creator_category_shoes)
        ItemCategory.ACCESSORIES -> stringResource(R.string.outfit_creator_category_accessories)
    }
}
