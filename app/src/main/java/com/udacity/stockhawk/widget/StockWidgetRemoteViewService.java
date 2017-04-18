package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by katsiarynamashokha on 4/4/17.
 */

public class StockWidgetRemoteViewService extends RemoteViewsService {
    static final int INDEX_WEATHER_ID = 0;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
                final long identityToken = Binder.clearCallingIdentity();
                data = getQuery();

                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                ArrayList<String> stockList = new ArrayList<>();
                stockList.add(Contract.Quote.COLUMN_SYMBOL);
                stockList.add(Contract.Quote.COLUMN_PRICE);
                stockList.add(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
                String[] stockArray = new String[stockList.size()];
                stockArray = stockList.toArray(stockArray);

                data = getContentResolver().query(
                        Contract.Quote.URI,
                        stockArray,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.stock_widget_detail_list_item);


                String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL - 1);
                Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE - 1);
                Float absoluteStockChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE - 1);


                views.setTextViewText(R.id.stock_name, stockSymbol);
                views.setTextViewText(R.id.stock_price, getCurrencySymbol() + stockPrice.toString());
                views.setContentDescription(R.id.stock_price, getString(R.string.stock_price_desc) + stockPrice);
                views.setTextViewText(R.id.stock_change, getCurrencySymbol() + absoluteStockChange.toString());
                if (absoluteStockChange >=0) {
                    views.setContentDescription(R.id.stock_change, getString(R.string.stock_increase_desc) + absoluteStockChange.toString());
                }
                else {
                    views.setContentDescription(R.id.stock_change, getString(R.string.stock_decrease_desc) + absoluteStockChange.toString());
                }

                final Intent fillIntent = new Intent();
                fillIntent.setData(Contract.Quote.makeUriForStock(stockSymbol));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillIntent);
                return views;
            }

            private Cursor getQuery() {
                ArrayList<String> stockList = new ArrayList<>();
                stockList.add(Contract.Quote.COLUMN_SYMBOL);
                stockList.add(Contract.Quote.COLUMN_PRICE);
                stockList.add(Contract.Quote.COLUMN_ABSOLUTE_CHANGE);
                String[] stockArray = new String[stockList.size()];
                stockArray = stockList.toArray(stockArray);

              return getContentResolver().query(
                        Contract.Quote.URI,
                        stockArray,
                        null,
                        null,
                        null);
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_WEATHER_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
    public String getCurrencySymbol() {
        Locale locale = new Locale("en", "US");
        Currency currency= Currency.getInstance(locale);
        return currency.getSymbol();
    }
}


