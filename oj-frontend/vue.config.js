const { defineConfig } = require("@vue/cli-service");
const MonacoWebpackPlugin = require("monaco-editor-webpack-plugin");
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
    proxy: {
      "/api": {
        target: "http://localhost:8121",
        changeOrigin: true
      }
    }
  },
  chainWebpack: (config) => {
    config.plugin("monaco").use(new MonacoWebpackPlugin({}));
  },
});
