const path = require('path')
const HtmlWebpackPlugin = require('html-webpack-plugin')

const HtmlWebpackPluginConfig = new HtmlWebpackPlugin({
  template: './client/main.html',
  filename: 'main.html',
  inject: 'body'
})

module.exports = {
  entry: './client/main.jsx',
  output: {
    path: path.resolve('dist'),
    filename: 'dev-build.js'
  },
  module: {
    loaders: [
      { test: /\.js$/, loader: 'babel-loader', exclude: /node_modules/ },
      { test: /\.jsx$/, loader: 'babel-loader', exclude: /node_modules/ }
    ]
  },
  "plugins": [HtmlWebpackPluginConfig]
}
