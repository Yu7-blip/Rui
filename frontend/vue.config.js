const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8080,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        logLevel: 'debug'
      }
    },
    // 最简单的方法：禁用historyApiFallback
    historyApiFallback: false
  },
  // 确保public目录正确
  publicPath: '/'
})