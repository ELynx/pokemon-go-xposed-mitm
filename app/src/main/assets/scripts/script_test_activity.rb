require 'ruboto/widget'
require 'ruboto/util/toast'

ruboto_import_widgets :Button, :LinearLayout, :TextView

class ScriptTestActivity
  def on_create(bundle)
    super
    set_title 'Hello Ruboto!'

    self.content_view = linear_layout(orientation: :vertical) do
      @text_view = text_view(
        text: 'What hath Matz wrought?',
        id: 42,
        width: :match_parent,
        gravity: :center,
        text_size: 48.0
      )

      button(
        text: 'M-x butterfly',
        width: :match_parent,
        id: 43,
        on_click_listener: ->() { butterfly }
      )
    end
  end

  private

  def butterfly
    @text_view.text = 'What hath Matz wrought!'
    toast 'Flipped a bit via butterfly'
  end
end
