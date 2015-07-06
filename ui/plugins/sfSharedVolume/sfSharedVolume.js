(function (cloudStack) {
  cloudStack.plugins.sfSharedVolume = function(plugin) {
    plugin.ui.addSection({
      id: 'sfSharedVolume',
      title: 'Shared Volume',
      preFilter: function(args) {
        return isAdmin();
      },
      show: function() {
        return $('<div>').html('Content will go here');
      }
    });
  };
}(cloudStack));
