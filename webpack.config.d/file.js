config.module.rules.push(
    {
        test: /\.(jpe?g|png|gif|svg)$/i,
        type: 'asset/resource'
    },
    {
        test: /\.xmldef$/i,
        type: 'asset/source'
    }
);
