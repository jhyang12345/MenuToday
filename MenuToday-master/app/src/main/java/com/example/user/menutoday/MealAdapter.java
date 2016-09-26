package com.example.user.menutoday;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by user on 2016-09-17.
 */
public class MealAdapter extends ArrayAdapter<Meal> {
    public MealAdapter(Context context, ArrayList<Meal> meals) {
        super(context, 0, meals);
    }

    static int totalheight = 0;
    class dishprice {
        String dishname;
        String price;
        public dishprice(String dishname, String price) {
            this.dishname = dishname;
            this.price = price;
        }
    }

    public String removeheader(String text) {
        String ret = text.trim();
        if(ret.charAt(0) == '[') {
            int last = 0;
            for(int i = 0; i < ret.length(); ++i) {
                if(ret.charAt(i) == ']') {
                    last = i;
                    return removeheader(ret.substring(i + 1));
                }
            }
            return ret.substring(last + 1);

        }
        return ret;
    }

    public String commaspacing(String text) {
        if(!text.contains(",")) {
            return text.trim();
        } else {
            int index = 0;
            text = text.trim();
            while(index < text.length()) {
                if (text.charAt(index) ==',' && index + 1 < text.length() && text.charAt(index + 1) != ' ') {
                    text = text.substring(0, index + 1) + ' ' + text.substring(index + 1);
                }
                index++;
            }
        }
        return text.trim();
    }

    private String simpleDish(String dishname) {
        String ret;
        System.out.println("Dishname at simpleDish: " + dishname);
        if(dishname.length() == 0) {
            return "";
        }
        if(dishname.indexOf("(") == 0 && dishname.indexOf(")") >= dishname.indexOf("(")) {
            ret = dishname.substring(dishname.indexOf("("), dishname.indexOf(")") + 1);
            if(ret.matches(".*[a-zA-Z]+.*")) {
                ret = ret.trim();
                ret = dishname.substring(dishname.indexOf(")") + 1);

                return simpleDish(commaspacing(removeheader(ret)));
            }

        } else if(dishname.indexOf("(") > 0 && dishname.indexOf(")") > dishname.indexOf("(")) {
            ret = dishname.substring(dishname.indexOf("("), dishname.indexOf(")") + 1);
            if(ret.matches(".*[a-zA-Z]+.*")) {
                ret = ret.trim();
                ret = dishname.substring(dishname.indexOf(")") + 1);

                return simpleDish(commaspacing(removeheader(ret)));
            }
        }
        return commaspacing(removeheader(dishname));
    }



    //inner adapter
    class dishpriceAdapter extends ArrayAdapter<dishprice> {
        dishpriceAdapter(Context context, ArrayList<dishprice> dishes) {
            super(context, 0, dishes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String dishname = getItem(position).dishname;
            String price = getItem(position).price;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dish, parent, false);

            /*convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("ListItem clicked!");
                    }
                }
            );*/
            }

            MainActivity.hasAdded.put(dishname, true);

            //setting to unclickable
            convertView.setEnabled(false);
            convertView.setOnClickListener(null);

            TextView dishName = (TextView) convertView.findViewById(R.id.dishName);
            TextView priceVal = (TextView) convertView.findViewById(R.id.price);

            dishName.setText(simpleDish(dishname));
            priceVal.setText(price);


            totalheight += convertView.getMeasuredHeight();
            System.out.println("Updating total height: " + totalheight);


//            ListView dishlist = (ListView) convertView.findViewById(R.id.dishList);

            return convertView;
        }
    }

    static class ViewHolderItem {
        TextView cafeteriaName;
        LinearLayout listview;
    }

    /*

     */


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String name = getItem(position).name;

        ViewHolderItem viewholder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.meal, parent, false);

            viewholder = new ViewHolderItem();

            viewholder.cafeteriaName = (TextView) convertView.findViewById(R.id.mealName);

            viewholder.listview = (LinearLayout) convertView.findViewById(R.id.dishList);

            convertView.setTag(viewholder);
        } else {
            viewholder = (ViewHolderItem) convertView.getTag();
        }

        TextView cafeteriaName = (TextView) convertView.findViewById(R.id.mealName);

        cafeteriaName.setText(name);

        AssetManager am = getContext().getAssets();

        Typeface typeface = Typeface.createFromAsset(am,
                String.format(Locale.KOREAN, "fonts/malgun.ttf", "fonts/malgun.ttf"));

        viewholder.cafeteriaName.setText(name);

        ArrayList<dishprice> dishlist = new ArrayList<dishprice>();

        for(int i = 0; i < getItem(position).dishes.size(); ++i) {

            dishlist.add(new dishprice(getItem(position).dishes.get(i), getItem(position).prices.get(i)));
            LinearLayout newitem =  (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.dish, parent, false);
            TextView dishname = (TextView) newitem.findViewById(R.id.dishName);
            TextView dishprice = (TextView) newitem.findViewById(R.id.price);


            //dishname.setTypeface(typeface);
            //dishprice.setTypeface(typeface);

            int pL = newitem.getPaddingLeft();
            int pT = newitem.getPaddingTop();
            int pR = newitem.getPaddingRight();
            int pB = newitem.getPaddingBottom();


            if(i == getItem(position).dishes.size() - 1) {
                newitem.setBackground(viewholder.listview.getResources().getDrawable(R.drawable.noborder));
                newitem.setPadding(pL, pT, pR, pB);
            } else {
                newitem.setBackground(viewholder.listview.getResources().getDrawable(R.drawable.borderbottom));
                newitem.setPadding(pL, pT, pR, pB);
            }

            dishname.setText(getItem(position).dishes.get(i));
            dishprice.setText(getItem(position).prices.get(i));
            viewholder.listview.addView(newitem);

        }
        //cafeteriaName.setTypeface(typeface);

        convertView.setOnClickListener(null);

        return convertView;
    }


}
