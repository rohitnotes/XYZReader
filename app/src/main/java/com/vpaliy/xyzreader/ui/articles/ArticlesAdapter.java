package com.vpaliy.xyzreader.ui.articles;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.vpaliy.xyzreader.R;
import com.vpaliy.xyzreader.domain.Article;
import com.vpaliy.xyzreader.ui.base.bus.RxBus;
import com.vpaliy.xyzreader.ui.base.bus.event.NavigationEvent;
import com.vpaliy.xyzreader.ui.view.ParallaxRatioImageView;
import com.vpaliy.xyzreader.ui.view.PresentationUtils;
import java.util.ArrayList;
import java.util.List;
import butterknife.ButterKnife;
import butterknife.BindView;
import static com.vpaliy.xyzreader.ui.articles.IArticlesConfig.ViewConfig;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {

    private List<Article> data;
    private LayoutInflater inflater;
    private Handler handler;
    private RxBus rxBus;
    private boolean isLocked;
    private ViewConfig config;

    ArticlesAdapter(Context context, RxBus rxBus){
        this.data=new ArrayList<>();
        this.handler=new Handler();
        this.rxBus=rxBus;
        this.inflater=LayoutInflater.from(context);
    }

    public void setData(List<Article> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setConfig(ViewConfig config) {
        this.config = config;
    }

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        @BindView(R.id.article_image)
        ParallaxRatioImageView image;

        @BindView(R.id.article_title)
        TextView articleTitle;

        @BindView(R.id.article_author)
        TextView articleAuthor;

        @BindView(R.id.details_background)
        View background;

        @BindView(R.id.article_date)
        TextView articleDate;

        ViewHolder(View root){
            super(root);
            ButterKnife.bind(this,root);
            root.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //if the user clicks more than once
            if(!isLocked) {
                isLocked=true;
                Article article = at(getAdapterPosition());
                rxBus.send(NavigationEvent.navigate(image,background,articleDate,
                        articleAuthor,articleTitle,itemView,article.getId()));
                //release after the details have been launched, 800ms should be enough
                handler.postDelayed(ArticlesAdapter.this::unlock,800);
            }
        }

        void bindData(){
            Article article=at(getAdapterPosition());
            articleTitle.setText(article.getTitle());
            articleAuthor.setText(article.getAuthor());
            articleDate.setText(article.getFormattedDate());
            Glide.with(itemView.getContext())
                    .load(article.getBackdropUrl())
                    .asBitmap()
                    .centerCrop()
                    .priority(Priority.IMMEDIATE)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(new ImageViewTarget<Bitmap>(image) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            image.setImageBitmap(resource);
                            //color the background
                            new Palette.Builder(resource)
                                    .generate(ViewHolder.this::applyPalette);
                        }
                    });
        }

        private void applyPalette(Palette palette){
            background.setBackgroundColor(PresentationUtils.getDominantColor(palette));
        }
    }

    private void unlock(){
        isLocked=!isLocked;
    }

    private Article at(int index){
        return data.get(index);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root=inflateView(parent);
        return new ViewHolder(root);
    }

    private View inflateView(ViewGroup parent){
        if(config==null||config==ViewConfig.GRID){
            return inflater.inflate(R.layout.adapter_article_item,parent,false);
        }
        return inflater.inflate(R.layout.adapter_mobile_article_item,parent,false);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
