package shacus.edu.seu.com.shacus.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import shacus.edu.seu.com.shacus.R;

/*
* 李嘉文 2016/8/26
* */

public class ImageAddGridViewAdapter extends BaseAdapter{

	private List<Drawable>list;
	private LayoutInflater inflater;
	public ImageAddGridViewAdapter(Context context, List<Drawable> list){
		inflater=LayoutInflater.from(context);
		this.list=list;
	}
	@Override
	public int getCount() {

		return list.size();
	}

	@Override
	public Object getItem(int position) {

		return list.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}
	public void changeList(List<Drawable>list){
		this.list=list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if(convertView==null){
			holder=new ViewHolder();
			convertView=inflater.inflate(R.layout.items_image_add_gridview, null);
			holder.imageView=(ImageView)convertView.findViewById(R.id.theme_picture);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
		holder.imageView.setImageDrawable(list.get(position));
		return convertView;
	}

	public class ViewHolder{
		ImageView imageView;
	}
}
