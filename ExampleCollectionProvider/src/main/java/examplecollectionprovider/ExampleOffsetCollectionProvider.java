/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package examplecollectionprovider;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryService;
import com.liferay.asset.kernel.service.persistence.AssetEntryQuery;
import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.pagination.InfoPage;
import com.liferay.info.sort.Sort;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;

import com.liferay.info.item.InfoItemServiceRegistry;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.service.persistence.AssetEntryQuery;
import com.liferay.info.pagination.Pagination;
import com.liferay.info.sort.Sort;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Portal;

import org.osgi.service.component.annotations.Reference;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;

import java.util.Collections;
import java.util.Locale;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pavel Savinov
 */
@Component(service = InfoCollectionProvider.class)
public class ExampleOffsetCollectionProvider
	implements InfoCollectionProvider<AssetEntry> {

	@Override
	public InfoPage<AssetEntry> getCollectionInfoPage(
		CollectionQuery collectionQuery) {

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		AssetEntryQuery assetEntryQuery = getAssetEntryQuery(
			serviceContext.getCompanyId(), serviceContext.getScopeGroupId(),
			collectionQuery.getPagination(), new Sort("viewCount", false),
			new Sort("title", true));

		try {

			List<AssetEntry> allAssets = _assetEntryService.getEntries(assetEntryQuery);
			List<AssetEntry> firstAssets = (List<AssetEntry>) allAssets.subList(0, 3);
			firstAssets.clear();

			return InfoPage.of(
				allAssets,
				collectionQuery.getPagination(),
				_assetEntryService.getEntriesCount(assetEntryQuery));
		}
		catch (Exception exception) {
			_log.error("Unable to get asset entries", exception);
		}

		return InfoPage.of(
			Collections.emptyList(), collectionQuery.getPagination(), 0);
	}

	protected AssetEntryQuery getAssetEntryQuery(
		long companyId, long groupId, Pagination pagination, Sort sort1,
		Sort sort2) {

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		// If you want to get all asset types
		long[] allClassIds = AssetRendererFactoryRegistryUtil.getClassNameIds(
			groupId, true);

		// If you want to get only WebContent
		long[] webContentClassId = new long[]{ClassNameLocalServiceUtil.getClassNameId("com.liferay.journal.model.JournalArticle")};

		assetEntryQuery.setClassNameIds(webContentClassId);

		assetEntryQuery.setEnablePermissions(true);

		if (pagination != null) {
			assetEntryQuery.setEnd(pagination.getEnd());
		}

		assetEntryQuery.setGroupIds(new long[] {groupId});
		assetEntryQuery.setListable(null);

		assetEntryQuery.setOrderByCol1(
			(sort1 != null) ? sort1.getFieldName() : Field.MODIFIED_DATE);
		assetEntryQuery.setOrderByCol2(
			(sort2 != null) ? sort2.getFieldName() : Field.CREATE_DATE);
		assetEntryQuery.setOrderByType1(
			(sort1 != null) ? _getOrderByType(sort1) : "DESC");
		assetEntryQuery.setOrderByType1(
			(sort2 != null) ? _getOrderByType(sort2) : "DESC");

		if (pagination != null) {
			assetEntryQuery.setStart(pagination.getStart());
		}

		return assetEntryQuery;
	}

	@Reference
	protected Portal portal;

	private String _getOrderByType(Sort sort) {
		if (sort.isReverse()) {
			return "DESC";
		}

		return "ASC";
	}

	@Override
	public String getLabel(Locale locale) {
		return "OFFSET 3 COLLECTION PROVIDER";
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ExampleCollectionProvider.class);

	@Reference
	private AssetEntryService _assetEntryService;

	@Reference
	private Language _language;

	@Reference
	private InfoItemServiceRegistry _infoItemServiceRegistry;

}
