config.module.rules.push(
    {
        test: /\.(jpe?g|png|gif|svg)$/i,
        loader: 'file-loader',
        options: {
              esModule: false,
        },
    },

);

config.module.rules.push(
    {
        test: /\.xmldef$/i,
        loader: 'raw-loader',
        options: {
                      esModule: false,
                },
    },

);